package chylex.hee.game.item
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.item.base.ItemBaseEnergyUser
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.render.util.ColorTransition
import chylex.hee.game.render.util.HCL
import chylex.hee.init.ModItems
import chylex.hee.system.util.Pos
import chylex.hee.system.util.angleBetween
import chylex.hee.system.util.center
import chylex.hee.system.util.closestTickingTile
import chylex.hee.system.util.distanceTo
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.over
import chylex.hee.system.util.toDegrees
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.EnumHand.OFF_HAND
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side.CLIENT
import net.minecraftforge.fml.relauncher.SideOnly

class ItemEnergyOracle : ItemBaseEnergyUser(){
	companion object{
		private const val ORACLE_IDENTIFIER_TAG = "ID"
		
		private const val TRACKED_CLUSTER_POS_TAG = "TrackingPos"
		private const val TRACKED_CLUSTER_HUE_TAG = "TrackingHue"
		private const val LAST_UPDATE_POS_TAG = "UpdatePos"
		
		private const val CLUSTER_DETECTION_RANGE = 96.0
		private const val CLUSTER_PROXIMITY_RANGE_MP = 1.0 / 6.0
		
		private const val CLUSTER_HUE_PROXIMITY_OVERRIDE = Short.MAX_VALUE
		
		private fun removeTrackedClusterTags(nbt: NBTTagCompound){
			nbt.removeTag(TRACKED_CLUSTER_POS_TAG)
			nbt.removeTag(TRACKED_CLUSTER_HUE_TAG)
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
		
		fun setupRecipeNBT(recipe: IRecipe){
			ModItems.ENERGY_ORACLE.setEnergyChargeLevel(recipe.recipeOutput, Units(20))
		}
	}
	
	init{
		addPropertyOverride(ResourceLocation(HardcoreEnderExpansion.ID, "activity_intensity")){
			stack, _, entity -> getActivityIntensityProp(stack, entity)
		}
	}
	
	// Energy properties
	
	override fun getEnergyCapacity(stack: ItemStack) =
		Units(75)
	
	override fun getEnergyPerUse(stack: ItemStack) =
		if (stack.heeTagOrNull?.hasKey(TRACKED_CLUSTER_POS_TAG) == true)
			3 over 20
		else
			2 over 20
	
	// Item handling
	
	override fun onUpdate(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		super.onUpdate(stack, world, entity, itemSlot, isSelected)
		
		if (world.isRemote){
			return
		}
		
		if (!isPlayerHolding(entity, stack)){
			stack.heeTagOrNull?.let(::removeTrackedClusterTags)
			return
		}
		
		// unique identifier
		
		if (!stack.heeTag.hasKey(ORACLE_IDENTIFIER_TAG)){
			stack.heeTag.setLong(ORACLE_IDENTIFIER_TAG, world.rand.nextLong())
		}
		
		// cluster detection
		
		if (world.totalWorldTime % 4L == 0L && hasAnyEnergy(stack)){
			val holderPos = Pos(entity)
			val closestCluster = holderPos.closestTickingTile<TileEntityEnergyCluster>(world, CLUSTER_DETECTION_RANGE)
			
			if (closestCluster == null){
				stack.heeTagOrNull?.let(::removeTrackedClusterTags)
			}
			else{
				with(stack.heeTag){
					setLong(TRACKED_CLUSTER_POS_TAG, closestCluster.pos.toLong())
					
					if (closestCluster.affectedByProximity && holderPos.distanceTo(closestCluster.pos) < CLUSTER_DETECTION_RANGE * CLUSTER_PROXIMITY_RANGE_MP){
						setShort(TRACKED_CLUSTER_HUE_TAG, CLUSTER_HUE_PROXIMITY_OVERRIDE)
					}
					else{
						setShort(TRACKED_CLUSTER_HUE_TAG, closestCluster.color.primaryHue)
					}
				}
			}
		}
		
		// energy usage
		
		if (world.totalWorldTime % 20L == 0L){
			val holderPos = Pos(entity)
			
			with(stack.heeTag){
				if (!hasKey(LAST_UPDATE_POS_TAG) || Pos(getLong(LAST_UPDATE_POS_TAG)) != holderPos){
					setLong(LAST_UPDATE_POS_TAG, holderPos.toLong())
					
					if (getShort(TRACKED_CLUSTER_HUE_TAG) != CLUSTER_HUE_PROXIMITY_OVERRIDE && !useEnergyUnit(stack)){
						removeTrackedClusterTags(this)
					}
				}
			}
		}
	}
	
	// Client
	
	// TODO tooltip could maybe show remaining time?
	// TODO re-equip animation seems too fast, there is very mild animation glitching
	
	@SideOnly(CLIENT)
	object Color: IItemColor{
		private const val NONE = -1
		
		private val INACTIVE =  HCL(0.0, 0F,   2.8F)
		private val SEARCHING = HCL(0.0, 0F,  68.0F)
		private val PROXIMITY = HCL(0.0, 0F, 100.0F)
		
		private val INACTIVE_INT = INACTIVE.toInt()
		
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
			val vecTarget = Pos(tag.getLong(TRACKED_CLUSTER_POS_TAG)).center.subtract(player.posX, player.posY + player.getEyeHeight(), player.posZ)
			
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
				return NONE
			}
			
			val tag = stack.heeTagOrNull ?: return INACTIVE_INT
			val player = HardcoreEnderExpansion.proxy.getClientSidePlayer() ?: return INACTIVE_INT
			
			val identifier = tag.getLong(ORACLE_IDENTIFIER_TAG)
			
			if (!itemMatches(identifier, stack, player.getHeldItem(MAIN_HAND)) && !itemMatches(identifier, stack, player.getHeldItem(OFF_HAND))){
				return INACTIVE_INT
			}
			
			if (transitionIdentifier != identifier){
				transitionIdentifier = identifier
				transitionQueue.resetAll()
			}
			
			setNextColor(determineNextColor(stack, tag, player))
			return transitionQueue.updateGetColor().toInt()
		}
	}
}
