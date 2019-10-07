package chylex.hee.game.item
import chylex.hee.client.render.util.ColorTransition
import chylex.hee.client.render.util.NO_TINT
import chylex.hee.client.util.MC
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.Infusion.CAPACITY
import chylex.hee.game.item.infusion.Infusion.DISTANCE
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.init.ModItems
import chylex.hee.system.Resource
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.Hand.OFF_HAND
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.Pos
import chylex.hee.system.util.angleBetween
import chylex.hee.system.util.center
import chylex.hee.system.util.closestTickingTile
import chylex.hee.system.util.color.HCL
import chylex.hee.system.util.distanceTo
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.getLongArray
import chylex.hee.system.util.getLongArrayOrNull
import chylex.hee.system.util.getPos
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.getTile
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.isLoaded
import chylex.hee.system.util.over
import chylex.hee.system.util.setLongArray
import chylex.hee.system.util.setPos
import chylex.hee.system.util.toDegrees
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet
import it.unimi.dsi.fastutil.longs.LongArrayList
import it.unimi.dsi.fastutil.longs.LongCollection
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ItemEnergyOracle : ItemAbstractEnergyUser(), IInfusableItem{
	companion object{
		private const val ORACLE_IDENTIFIER_TAG = "ID"
		private const val ORACLE_LAST_SLOT_TAG = "Slot"
		
		private const val TRACKED_CLUSTER_POS_TAG = "TrackingPos"
		private const val TRACKED_CLUSTER_HUE_TAG = "TrackingHue"
		private const val LAST_UPDATE_POS_TAG = "UpdatePos"
		private const val IGNORED_CLUSTERS_TAG = "IgnoreList"
		
		private const val CLUSTER_DETECTION_RANGE_BASE = 96.0
		private const val CLUSTER_PROXIMITY_RANGE_MP = 1.0 / 6.0
		
		private const val CLUSTER_HUE_PROXIMITY_OVERRIDE = Short.MAX_VALUE
		
		private fun removeTrackedClusterTags(nbt: NBTTagCompound){
			nbt.removeTag(TRACKED_CLUSTER_POS_TAG)
			nbt.removeTag(TRACKED_CLUSTER_HUE_TAG)
		}
		
		private fun updateIgnoredClusterTag(nbt: NBTTagCompound, newIgnoreList: LongCollection){
			if (newIgnoreList.isEmpty()){
				nbt.removeTag(IGNORED_CLUSTERS_TAG)
			}
			else{
				nbt.setLongArray(IGNORED_CLUSTERS_TAG, newIgnoreList.toLongArray()) // the collection must be sorted
			}
		}
		
		private fun isPlayerHolding(entity: Entity?, stack: ItemStack): Boolean{
			return entity is EntityPlayer && (entity.getHeldItem(MAIN_HAND) === stack || entity.getHeldItem(OFF_HAND) === stack)
		}
		
		private fun getActivityIntensityProp(stack: ItemStack, entity: EntityLivingBase?): Float{
			val tag = stack.heeTagOrNull
			
			return if (tag == null || !isPlayerHolding(entity, stack)){
				0.0F // inactive
			}
			else if (!tag.hasKey(TRACKED_CLUSTER_HUE_TAG)){
				1.0F // searching
			}
			else if (tag.getShort(TRACKED_CLUSTER_HUE_TAG) == CLUSTER_HUE_PROXIMITY_OVERRIDE){
				0.0F // proximity
			}
			else{
				0.5F // tracking
			}
		}
		
		fun setupRecipeNBT(stack: ItemStack){
			ModItems.ENERGY_ORACLE.setEnergyChargeLevel(stack, Units(30))
		}
	}
	
	init{
		addPropertyOverride(Resource.Custom("activity_intensity")){
			stack, _, entity -> getActivityIntensityProp(stack, entity)
		}
	}
	
	// Energy properties
	
	override fun getEnergyCapacity(stack: ItemStack) =
		Units((75 * InfusionTag.getList(stack).calculateLevelMultiplier(CAPACITY, 2F)).floorToInt())
	
	override fun getEnergyPerUse(stack: ItemStack) =
		if (stack.heeTagOrNull.hasKey(TRACKED_CLUSTER_POS_TAG))
			3 over 24
		else
			2 over 24
	
	// Cluster helpers
	
	private fun getIgnoredClusters(stack: ItemStack): LongArray?{
		return stack.takeIf { it.item === this }?.heeTagOrNull?.getLongArrayOrNull(IGNORED_CLUSTERS_TAG)
	}
	
	private fun isClusterEntryInvalid(world: World, entry: Long): Boolean{
		return Pos(entry).let { it.isLoaded(world) && it.getTile<TileEntityEnergyCluster>(world) == null }
	}
	
	fun isClusterIgnored(player: EntityPlayer, pos: BlockPos): Boolean{
		val entry = pos.toLong()
		
		return (
			(getIgnoredClusters(player.getHeldItem(MAIN_HAND))?.binarySearch(entry) ?: -1) >= 0 ||
			(getIgnoredClusters(player.getHeldItem(OFF_HAND))?.binarySearch(entry) ?: -1) >= 0
		)
	}
	
	private fun getClusterDetectionRange(stack: ItemStack) =
		CLUSTER_DETECTION_RANGE_BASE * InfusionTag.getList(stack).calculateLevelMultiplier(DISTANCE, 1.5F)
	
	// Item handling
	
	override fun canApplyInfusion(infusion: Infusion): Boolean{
		return ItemAbstractInfusable.onCanApplyInfusion(this, infusion)
	}
	
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		if (player.isSneaking && pos.getTile<TileEntityEnergyCluster>(world) != null){
			if (world.isRemote){
				return SUCCESS
			}
			
			val heldItem = player.getHeldItem(hand)
			val entry = pos.toLong()
			
			with(heldItem.heeTag){
				val ignoreList = LongAVLTreeSet(getLongArray(IGNORED_CLUSTERS_TAG))
				
				if (!ignoreList.add(entry)){
					ignoreList.remove(entry)
				}
				
				updateIgnoredClusterTag(this, ignoreList)
			}
			
			return SUCCESS
		}
		
		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ)
	}
	
	override fun onUpdate(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		super.onUpdate(stack, world, entity, itemSlot, isSelected)
		
		if (world.isRemote){
			return
		}
		
		val tag = stack.heeTag
		val currentTime = world.totalWorldTime
		
		// unique identifier
		
		if (tag.getIntegerOrNull(ORACLE_LAST_SLOT_TAG) != itemSlot){
			tag.setLong(ORACLE_IDENTIFIER_TAG, world.rand.nextLong())
			tag.setInteger(ORACLE_LAST_SLOT_TAG, itemSlot)
		}
		
		if (currentTime % 200L == 0L){
			val ignoreList = tag.getLongArrayOrNull(IGNORED_CLUSTERS_TAG)
			
			if (ignoreList != null && ignoreList.any { isClusterEntryInvalid(world, it) }){
				val newIgnoreList = LongArrayList(ignoreList.size - 1)
				
				for(entry in ignoreList){
					if (!isClusterEntryInvalid(world, entry)){
						newIgnoreList.add(entry)
					}
				}
				
				updateIgnoredClusterTag(tag, newIgnoreList)
			}
		}
		
		// cluster cleanup
		
		if (!isPlayerHolding(entity, stack)){
			removeTrackedClusterTags(tag)
			return
		}
		
		// cluster detection
		
		if (currentTime % 4L == 0L && hasAnyEnergy(stack)){
			val holderPos = Pos(entity)
			val detectionRange = getClusterDetectionRange(stack)
			val ignoreList = tag.getLongArray(IGNORED_CLUSTERS_TAG)
			
			val closestCluster = holderPos.closestTickingTile<TileEntityEnergyCluster>(world, detectionRange){
				ignoreList.binarySearch(it.pos.toLong()) < 0
			}
			
			if (closestCluster == null){
				removeTrackedClusterTags(tag)
			}
			else{
				with(tag){
					setPos(TRACKED_CLUSTER_POS_TAG, closestCluster.pos)
					
					if (closestCluster.affectedByProximity && holderPos.distanceTo(closestCluster.pos) < detectionRange * CLUSTER_PROXIMITY_RANGE_MP){
						setShort(TRACKED_CLUSTER_HUE_TAG, CLUSTER_HUE_PROXIMITY_OVERRIDE)
					}
					else{
						setShort(TRACKED_CLUSTER_HUE_TAG, closestCluster.color.primaryHue)
					}
				}
			}
		}
		
		// energy usage
		
		if (currentTime % 40L == 0L){
			val holderPos = Pos(entity)
			
			with(tag){
				if (getPosOrNull(LAST_UPDATE_POS_TAG) != holderPos){
					setPos(LAST_UPDATE_POS_TAG, holderPos)
					
					if (getShort(TRACKED_CLUSTER_HUE_TAG) != CLUSTER_HUE_PROXIMITY_OVERRIDE && !useEnergyUnit(stack)){
						removeTrackedClusterTags(this)
					}
				}
			}
		}
	}
	
	// Client side
	
	// TODO tooltip could maybe show remaining time?
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean{
		return (oldStack.item === this) != (newStack.item === this) // disabling the animation looks a bit nicer, otherwise it happens a bit too fast
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		ItemAbstractInfusable.onAddInformation(stack, lines)
	}
	
	@Sided(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean{
		return super.hasEffect(stack) // infusion glint is way too strong and obscures the core
	}
	
	@Sided(Side.CLIENT)
	object Color : IItemColor{
		private val INACTIVE  = HCL(0.0, 0F,   2.8F)
		private val SEARCHING = HCL(0.0, 0F,  68.0F)
		private val PROXIMITY = HCL(0.0, 0F, 100.0F)
		
		private val INACTIVE_INT = INACTIVE.i
		
		private val transitionQueue = ColorTransition(INACTIVE, 200F)
		private var transitionIdentifier = 0L
		
		private fun itemMatches(identifier: Long, renderer: ItemStack, held: ItemStack): Boolean{
			if (renderer === held){
				return true
			}
			
			if (renderer.item === held.item){
				return identifier == held.heeTagOrNull?.getLong(ORACLE_IDENTIFIER_TAG)
			}
			
			return false
		}
		
		private fun setNextColor(next: HCL){
			val current = transitionQueue.currentTargetColor
			
			if (next != current && next != transitionQueue.lastColorInQueue){
				transitionQueue.resetQueue()
				
				if (next.hue != current.hue && (next.chroma != 0F || current.chroma != 0F)){
					transitionQueue.enqueue(SEARCHING.copy(hue = next.hue))
				}
				
				transitionQueue.enqueue(next)
			}
		}
		
		private fun determineNextColor(stack: ItemStack, tag: NBTTagCompound, player: EntityPlayer): HCL{
			if (!ModItems.ENERGY_ORACLE.hasAnyEnergy(stack)){
				return INACTIVE
			}
			
			if (!tag.hasKey(TRACKED_CLUSTER_POS_TAG)){
				return SEARCHING
			}
			
			val clusterHue = tag.getShort(TRACKED_CLUSTER_HUE_TAG)
			
			if (clusterHue == CLUSTER_HUE_PROXIMITY_OVERRIDE){
				return PROXIMITY
			}
			
			val vecLook = player.lookVec
			val vecTarget = tag.getPos(TRACKED_CLUSTER_POS_TAG).center.subtract(player.posX, player.posY + player.getEyeHeight(), player.posZ)
			
			val angleDifference = vecLook.angleBetween(vecTarget).toDegrees()
			
			val level = when{
				angleDifference <  15 -> 99F
				angleDifference <  45 -> 75F
				angleDifference <  75 -> 60F
				angleDifference < 105 -> 50F
				else                  -> 40F
			}
			
			return HCL(clusterHue.toDouble(), level, (25 + level * 3) / 4)
		}
		
		override fun colorMultiplier(stack: ItemStack, tintIndex: Int): Int{
			if (tintIndex != 1){
				return NO_TINT
			}
			
			val tag = stack.heeTagOrNull ?: return INACTIVE_INT
			val player = MC.player
			
			if (player == null || !MC.renderManager.isRenderShadow){ // do not render on player model in inventory // UPDATE: make sure RenderManager.renderShadow is still only affected by GuiInventory
				return INACTIVE_INT
			}
			
			val identifier = tag.getLong(ORACLE_IDENTIFIER_TAG)
			
			if (!itemMatches(identifier, stack, player.getHeldItem(MAIN_HAND)) && !itemMatches(identifier, stack, player.getHeldItem(OFF_HAND))){
				return INACTIVE_INT
			}
			
			if (transitionIdentifier != identifier){
				transitionIdentifier = identifier
				transitionQueue.resetAll()
			}
			
			setNextColor(determineNextColor(stack, tag, player))
			return transitionQueue.updateGetColor().i
		}
	}
}
