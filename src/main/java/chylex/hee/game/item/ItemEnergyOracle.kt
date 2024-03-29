package chylex.hee.game.item

import chylex.hee.client.color.ColorTransition
import chylex.hee.client.util.MC
import chylex.hee.game.Resource
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.item.ItemAbstractEnergyUser.EnergyItem
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.EnchantmentGlintComponent
import chylex.hee.game.item.components.IReequipAnimationComponent
import chylex.hee.game.item.components.ITickInInventoryComponent
import chylex.hee.game.item.components.PlayerUseItemOnBlockComponent
import chylex.hee.game.item.infusion.Infusion.CAPACITY
import chylex.hee.game.item.infusion.Infusion.DISTANCE
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.properties.ItemTint
import chylex.hee.game.item.util.ItemProperty
import chylex.hee.game.mechanics.energy.IClusterOracleItem
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.world.util.closestTickingTile
import chylex.hee.game.world.util.distanceTo
import chylex.hee.game.world.util.getTile
import chylex.hee.game.world.util.isLoaded
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.color.space.HCL
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.Pos
import chylex.hee.util.math.angleBetween
import chylex.hee.util.math.center
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.over
import chylex.hee.util.math.toDegrees
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getIntegerOrNull
import chylex.hee.util.nbt.getLongArrayOrNull
import chylex.hee.util.nbt.getPos
import chylex.hee.util.nbt.getPosOrNull
import chylex.hee.util.nbt.hasKey
import chylex.hee.util.nbt.putPos
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet
import it.unimi.dsi.fastutil.longs.LongArrayList
import it.unimi.dsi.fastutil.longs.LongCollection
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.Hand.OFF_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.apache.commons.lang3.math.Fraction

object ItemEnergyOracle : HeeItemBuilder() {
	private const val ORACLE_IDENTIFIER_TAG = "ID"
	private const val ORACLE_LAST_SLOT_TAG = "Slot"
	
	private const val TRACKED_CLUSTER_POS_TAG = "TrackingPos"
	private const val TRACKED_CLUSTER_HUE_TAG = "TrackingHue"
	private const val LAST_UPDATE_POS_TAG = "UpdatePos"
	private const val IGNORED_CLUSTERS_TAG = "IgnoreList"
	
	private const val CLUSTER_DETECTION_RANGE_BASE = 96.0
	private const val CLUSTER_PROXIMITY_RANGE_MP = 1.0 / 6.0
	
	private const val CLUSTER_HUE_PROXIMITY_OVERRIDE = Short.MAX_VALUE
	
	private val ACTIVITY_INTENSITY_PROPERTY = ItemProperty(Resource.Custom("activity_intensity")) { stack, entity ->
		val tag = stack.heeTagOrNull
		
		if (tag == null || !isPlayerHolding(entity, stack)) {
			0.0F // inactive
		}
		else if (!tag.hasKey(TRACKED_CLUSTER_HUE_TAG)) {
			1.0F // searching
		}
		else if (tag.getShort(TRACKED_CLUSTER_HUE_TAG) == CLUSTER_HUE_PROXIMITY_OVERRIDE) {
			0.0F // proximity
		}
		else {
			0.5F // tracking
		}
	}
	
	val ENERGY = object : EnergyItem() {
		override fun getEnergyCapacity(stack: ItemStack): Units {
			return Units((75 * InfusionTag.getList(stack).calculateLevelMultiplier(CAPACITY, 2F)).floorToInt())
		}
		
		override fun getEnergyPerUse(stack: ItemStack): Fraction {
			return if (stack.heeTagOrNull.hasKey(TRACKED_CLUSTER_POS_TAG))
				3 over 24
			else
				2 over 24
		}
	}
	
	init {
		includeFrom(ItemAbstractInfusable())
		includeFrom(ItemAbstractEnergyUser(ENERGY))
		
		model = ItemModel.WithOverrides(
			ItemModel.Layers("energy_oracle", "energy_oracle_indicator_inactive"),
			ACTIVITY_INTENSITY_PROPERTY to mapOf(
				0.5F to ItemModel.Suffixed("_active_mild", ItemModel.Layers("energy_oracle", "energy_oracle_indicator_active_mild")),
				1.0F to ItemModel.Suffixed("_active_full", ItemModel.Layers("energy_oracle", "energy_oracle_indicator_active_full")),
			)
		)
		
		tint = Tint
		
		properties.add(ACTIVITY_INTENSITY_PROPERTY)
		
		maxStackSize = 1
		
		// POLISH tooltip could maybe show remaining time?
		
		components.glint = EnchantmentGlintComponent // infusion glint is way too strong and obscures the core
		
		components.reequipAnimation = object : IReequipAnimationComponent {
			override fun shouldAnimate(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean {
				return oldStack.item !== newStack.item // disabling the animation looks a bit nicer, otherwise it happens a bit too fast
			}
		}
		
		val originalUseOnBlock = components.useOnBlock!!
		components.useOnBlock = object : PlayerUseItemOnBlockComponent() {
			override fun use(world: World, pos: BlockPos, player: PlayerEntity, heldItem: ItemStack, context: ItemUseContext): ActionResultType {
				if (player.isSneaking && pos.getTile<TileEntityEnergyCluster>(world) != null) {
					if (world.isRemote) {
						return SUCCESS
					}
					
					val entry = pos.toLong()
					
					with(heldItem.heeTag) {
						val ignoreList = LongAVLTreeSet(getLongArray(IGNORED_CLUSTERS_TAG))
						
						if (!ignoreList.add(entry)) {
							ignoreList.remove(entry)
						}
						
						updateIgnoredClusterTag(this, ignoreList)
					}
					
					return SUCCESS
				}
				
				return originalUseOnBlock.use(world, pos, context)
			}
		}
		
		components.tickInInventory.add(object : ITickInInventoryComponent {
			override fun tick(world: World, entity: Entity, stack: ItemStack, slot: Int, isSelected: Boolean) {
				if (world.isRemote) {
					return
				}
				
				val tag = stack.heeTag
				val currentTime = world.gameTime
				
				// unique identifier
				
				if (tag.getIntegerOrNull(ORACLE_LAST_SLOT_TAG) != slot) {
					tag.putLong(ORACLE_IDENTIFIER_TAG, world.rand.nextLong())
					tag.putInt(ORACLE_LAST_SLOT_TAG, slot)
				}
				
				if (currentTime % 200L == 0L) {
					val ignoreList = tag.getLongArrayOrNull(IGNORED_CLUSTERS_TAG)
					
					if (ignoreList != null && ignoreList.any { isClusterEntryInvalid(world, it) }) {
						val newIgnoreList = LongArrayList(ignoreList.size - 1)
						
						for (entry in ignoreList) {
							if (!isClusterEntryInvalid(world, entry)) {
								newIgnoreList.add(entry)
							}
						}
						
						updateIgnoredClusterTag(tag, newIgnoreList)
					}
				}
				
				// cluster cleanup
				
				if (!isPlayerHolding(entity, stack)) {
					removeTrackedClusterTags(tag)
					return
				}
				
				// cluster detection
				
				if (currentTime % 4L == 0L && ENERGY.hasAnyEnergy(stack)) {
					val holderPos = Pos(entity)
					val detectionRange = getClusterDetectionRange(stack)
					val ignoreList = tag.getLongArray(IGNORED_CLUSTERS_TAG)
					
					val closestCluster = holderPos.closestTickingTile<TileEntityEnergyCluster>(world, detectionRange) {
						ignoreList.binarySearch(it.pos.toLong()) < 0
					}
					
					if (closestCluster == null) {
						removeTrackedClusterTags(tag)
					}
					else {
						with(tag) {
							putPos(TRACKED_CLUSTER_POS_TAG, closestCluster.pos)
							
							if (closestCluster.affectedByProximity && holderPos.distanceTo(closestCluster.pos) < detectionRange * CLUSTER_PROXIMITY_RANGE_MP) {
								putShort(TRACKED_CLUSTER_HUE_TAG, CLUSTER_HUE_PROXIMITY_OVERRIDE)
							}
							else {
								putShort(TRACKED_CLUSTER_HUE_TAG, closestCluster.color.primaryHue)
							}
						}
					}
				}
				
				// energy usage
				
				if (currentTime % 40L == 0L) {
					val holderPos = Pos(entity)
					
					with(tag) {
						if (getPosOrNull(LAST_UPDATE_POS_TAG) != holderPos) {
							putPos(LAST_UPDATE_POS_TAG, holderPos)
							
							if (getShort(TRACKED_CLUSTER_HUE_TAG) != CLUSTER_HUE_PROXIMITY_OVERRIDE && !ENERGY.useUnit(entity, stack)) {
								removeTrackedClusterTags(this)
							}
						}
					}
				}
			}
		})
		
		interfaces[IClusterOracleItem::class.java] = object : IClusterOracleItem {
			override fun isPositionIgnored(stack: ItemStack, pos: BlockPos): Boolean {
				val ignored = stack.heeTagOrNull?.getLongArrayOrNull(IGNORED_CLUSTERS_TAG)
				return ignored != null && ignored.binarySearch(pos.toLong()) >= 0
			}
		}
	}
	
	private fun removeTrackedClusterTags(nbt: TagCompound) {
		nbt.remove(TRACKED_CLUSTER_POS_TAG)
		nbt.remove(TRACKED_CLUSTER_HUE_TAG)
	}
	
	private fun updateIgnoredClusterTag(nbt: TagCompound, newIgnoreList: LongCollection) {
		if (newIgnoreList.isEmpty()) {
			nbt.remove(IGNORED_CLUSTERS_TAG)
		}
		else {
			nbt.putLongArray(IGNORED_CLUSTERS_TAG, newIgnoreList.toLongArray()) // the collection must be sorted
		}
	}
	
	private fun isPlayerHolding(entity: Entity?, stack: ItemStack): Boolean {
		return entity is PlayerEntity && (entity.getHeldItem(MAIN_HAND) === stack || entity.getHeldItem(OFF_HAND) === stack)
	}
	
	// Cluster helpers
	
	private fun isClusterEntryInvalid(world: World, entry: Long): Boolean {
		return Pos(entry).let { it.isLoaded(world) && it.getTile<TileEntityEnergyCluster>(world) == null }
	}
	
	private fun getClusterDetectionRange(stack: ItemStack) =
		CLUSTER_DETECTION_RANGE_BASE * InfusionTag.getList(stack).calculateLevelMultiplier(DISTANCE, 1.5F)
	
	// Client side
	
	object Tint : ItemTint() {
		@JvmField
		var isRenderingInventoryEntity = false
		
		private val INACTIVE  = HCL(0.0, 0F,   2.8F)
		private val SEARCHING = HCL(0.0, 0F,  68.0F)
		private val PROXIMITY = HCL(0.0, 0F, 100.0F)
		
		private val INACTIVE_INT = INACTIVE.i
		
		private val transitionQueue = ColorTransition(INACTIVE, 200F)
		private var transitionIdentifier = 0L
		
		private fun itemMatches(identifier: Long, renderer: ItemStack, held: ItemStack): Boolean {
			if (renderer === held) {
				return true
			}
			
			if (renderer.item === held.item) {
				return identifier == held.heeTagOrNull?.getLong(ORACLE_IDENTIFIER_TAG)
			}
			
			return false
		}
		
		private fun setNextColor(next: HCL) {
			val current = transitionQueue.currentTargetColor
			
			if (next != current && next != transitionQueue.lastColorInQueue) {
				transitionQueue.resetQueue()
				
				if (next.hue != current.hue && (next.chroma != 0F || current.chroma != 0F)) {
					transitionQueue.enqueue(SEARCHING.copy(hue = next.hue))
				}
				
				transitionQueue.enqueue(next)
			}
		}
		
		private fun determineNextColor(stack: ItemStack, tag: TagCompound, player: PlayerEntity): HCL {
			if (!ENERGY.hasAnyEnergy(stack)) {
				return INACTIVE
			}
			
			if (!tag.hasKey(TRACKED_CLUSTER_POS_TAG)) {
				return SEARCHING
			}
			
			val clusterHue = tag.getShort(TRACKED_CLUSTER_HUE_TAG)
			if (clusterHue == CLUSTER_HUE_PROXIMITY_OVERRIDE) {
				return PROXIMITY
			}
			
			val vecLook = player.lookVec
			val vecTarget = tag.getPos(TRACKED_CLUSTER_POS_TAG).center.subtract(player.posX, player.posY + player.eyeHeight, player.posZ)
			
			val angleDifference = vecLook.angleBetween(vecTarget).toDegrees()
			
			val level = when {
				angleDifference <  15 -> 99F
				angleDifference <  45 -> 75F
				angleDifference <  75 -> 60F
				angleDifference < 105 -> 50F
				else                  -> 40F
			}
			
			return HCL(clusterHue.toDouble(), level, (25 + level * 3) / 4)
		}
		
		@Sided(Side.CLIENT)
		override fun tint(stack: ItemStack, tintIndex: Int): Int {
			if (tintIndex != 1) {
				return NO_TINT
			}
			
			val tag = stack.heeTagOrNull ?: return INACTIVE_INT
			val player = MC.player
			
			if (player == null || isRenderingInventoryEntity) {
				return INACTIVE_INT
			}
			
			val identifier = tag.getLong(ORACLE_IDENTIFIER_TAG)
			
			if (!itemMatches(identifier, stack, player.getHeldItem(MAIN_HAND)) && !itemMatches(identifier, stack, player.getHeldItem(OFF_HAND))) {
				return INACTIVE_INT
			}
			
			if (transitionIdentifier != identifier) {
				transitionIdentifier = identifier
				transitionQueue.resetAll()
			}
			
			setNextColor(determineNextColor(stack, tag, player))
			return transitionQueue.updateGetColor().i
		}
	}
}
