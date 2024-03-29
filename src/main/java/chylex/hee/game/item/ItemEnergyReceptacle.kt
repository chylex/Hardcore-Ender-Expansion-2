package chylex.hee.game.item

import chylex.hee.HEE
import chylex.hee.game.Resource
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.IReequipAnimationComponent
import chylex.hee.game.item.components.ITickInInventoryComponent
import chylex.hee.game.item.components.ITooltipComponent
import chylex.hee.game.item.components.PlayerUseItemOnBlockComponent
import chylex.hee.game.item.infusion.Infusion.SAFETY
import chylex.hee.game.item.infusion.Infusion.STABILITY
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.properties.ItemTint
import chylex.hee.game.item.util.ItemProperty
import chylex.hee.game.mechanics.energy.ClusterSnapshot
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.displayString
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Floating
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Internal
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.world.isEndDimension
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModBlocks
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.Pos
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getDecodedOrNull
import chylex.hee.util.nbt.getIntegerOrNull
import chylex.hee.util.nbt.hasKey
import chylex.hee.util.nbt.putEncoded
import chylex.hee.util.nbt.use
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import kotlin.math.pow

object ItemEnergyReceptacle : HeeItemBuilder() {
	private const val CLUSTER_SNAPSHOT_TAG = "Cluster"
	private const val UPDATE_TIME_TAG = "UpdateTime"
	private const val RENDER_COLOR_TAG = "RenderColor"
	
	private const val INITIAL_LEVEL_TAG = "OrigLevel"
	private const val INITIAL_DIMENSION_TAG = "OrigDim"
	private const val INITIAL_TERRITORY_TAG = "OrigTerritory"
	
	private const val LANG_TOOLTIP_EMPTY = "item.hee.energy_receptacle.tooltip.empty"
	private const val LANG_TOOLTIP_HOLDING = "item.hee.energy_receptacle.tooltip.holding"
	
	private const val ENERGY_LOSS_TICK_RATE = 10L
	private const val ITEM_COOLDOWN = 16
	
	private val HAS_CLUSTER_PROPERTY = ItemProperty(Resource.Custom("has_cluster")) { stack ->
		if (stack.heeTagOrNull.hasKey(CLUSTER_SNAPSHOT_TAG)) 1F else 0F // POLISH tweak animation
	}
	
	init {
		includeFrom(ItemAbstractInfusable())
		
		localizationExtra[LANG_TOOLTIP_EMPTY] = "§9Empty"
		localizationExtra[LANG_TOOLTIP_HOLDING] = "§9Holding §3%s§9 Energy"
		
		model = ItemModel.WithOverrides(
			ItemModel.Simple,
			HAS_CLUSTER_PROPERTY to mapOf(
				1F to ItemModel.Suffixed("_with_cluster", ItemModel.Layers("energy_receptacle", "energy_receptacle_cluster"))
			)
		)
		
		tint = object : ItemTint() {
			@Sided(Side.CLIENT)
			override fun tint(stack: ItemStack, tintIndex: Int) = when (tintIndex) {
				1    -> stack.heeTagOrNull?.getInt(RENDER_COLOR_TAG) ?: WHITE
				else -> NO_TINT
			}
		}
		
		properties.add(HAS_CLUSTER_PROPERTY)
		
		maxStackSize = 1
		
		components.tooltip.add(0, ITooltipComponent { lines, stack, _, world ->
			if (world != null) {
				val tag = stack.heeTagOrNull
				
				if (!tag.hasKey(CLUSTER_SNAPSHOT_TAG)) {
					lines.add(TranslationTextComponent(LANG_TOOLTIP_EMPTY))
				}
				else {
					val snapshot = ClusterSnapshot(tag.getCompound(CLUSTER_SNAPSHOT_TAG))
					val level = calculateNewEnergyLevel(snapshot, world.gameTime - tag.getLong(UPDATE_TIME_TAG), InfusionTag.getList(stack))
					
					lines.add(TranslationTextComponent(LANG_TOOLTIP_HOLDING, level.displayString))
				}
			}
		})
		
		components.reequipAnimation = IReequipAnimationComponent.AnimateIfSlotChanged
		
		components.useOnBlock = object : PlayerUseItemOnBlockComponent() {
			override fun use(world: World, pos: BlockPos, player: PlayerEntity, heldItem: ItemStack, context: ItemUseContext): ActionResultType {
				heldItem.heeTag.use {
					if (hasKey(CLUSTER_SNAPSHOT_TAG)) {
						val finalPos = BlockEditor.place(ModBlocks.ENERGY_CLUSTER, player, heldItem, context)
						
						if (world.isRemote) {
							return SUCCESS
						}
						
						if (finalPos != null) {
							finalPos.getTile<TileEntityEnergyCluster>(world)?.let {
								it.loadClusterSnapshot(ClusterSnapshot(getCompound(CLUSTER_SNAPSHOT_TAG)), inactive = false)
								
								if (shouldLoseHealth(it, this, InfusionTag.getList(heldItem))) {
									it.deteriorateHealth()
								}
							}
							
							remove(CLUSTER_SNAPSHOT_TAG)
							remove(UPDATE_TIME_TAG)
							remove(RENDER_COLOR_TAG)
							remove(INITIAL_LEVEL_TAG)
							remove(INITIAL_DIMENSION_TAG)
							
							player.cooldownTracker.setCooldown(heldItem.item, ITEM_COOLDOWN)
							return SUCCESS
						}
					}
					else {
						if (world.isRemote) {
							return SUCCESS
						}
						
						val cluster = pos.getTile<TileEntityEnergyCluster>(world)
						
						if (cluster != null && cluster.tryDisturb()) {
							put(CLUSTER_SNAPSHOT_TAG, cluster.getClusterSnapshot().tag)
							
							putLong(UPDATE_TIME_TAG, world.gameTime)
							putInt(RENDER_COLOR_TAG, cluster.color.primary(75F, 80F).i)
							
							putInt(INITIAL_LEVEL_TAG, cluster.energyLevel.internal.value)
							putEncoded(INITIAL_DIMENSION_TAG, world.dimensionKey, World.CODEC, HEE.log)
							
							if (world.isEndDimension) {
								TerritoryInstance.fromPos(pos)?.let { putInt(INITIAL_TERRITORY_TAG, it.hash) }
							}
							
							cluster.breakWithoutExplosion = true
							pos.breakBlock(world, false)
							
							player.cooldownTracker.setCooldown(heldItem.item, ITEM_COOLDOWN)
							return SUCCESS
						}
					}
				}
				
				return FAIL
			}
		}
		
		components.tickInInventory.add(object : ITickInInventoryComponent {
			override fun tick(world: World, entity: Entity, stack: ItemStack, slot: Int, isSelected: Boolean) {
				if (world.isRemote) {
					return
				}
				
				with(stack.heeTagOrNull ?: return) {
					if (!hasKey(CLUSTER_SNAPSHOT_TAG)) {
						return
					}
					
					val currentTime = world.gameTime
					val ticksElapsed = currentTime - getLong(UPDATE_TIME_TAG)
					
					if (ticksElapsed < ENERGY_LOSS_TICK_RATE) {
						return
					}
					
					val snapshot = ClusterSnapshot(getCompound(CLUSTER_SNAPSHOT_TAG))
					val newLevel = calculateNewEnergyLevel(snapshot, ticksElapsed, InfusionTag.getList(stack))
					
					put(CLUSTER_SNAPSHOT_TAG, snapshot.clone(energyLevel = newLevel).tag)
					putLong(UPDATE_TIME_TAG, currentTime)
					
					if (hasMovedTooFar(this, world, Pos(entity))) { // force health deterioration
						putInt(INITIAL_DIMENSION_TAG, Int.MIN_VALUE)
						remove(INITIAL_TERRITORY_TAG)
					}
				}
			}
		})
	}
	
	private fun calculateNewEnergyLevel(snapshot: ClusterSnapshot, elapsedTicks: Long, infusions: InfusionList): IEnergyQuantity {
		// TODO make sure Table Pedestals keep updating the item, or at least perform an update just before the infusion
		val power = if (infusions.has(STABILITY)) 0.0003F else 0.001F
		
		val decreasePerCycle = Floating(snapshot.energyCapacity.floating.value.pow(power) - 1F)
		val elapsedCycles = elapsedTicks / ENERGY_LOSS_TICK_RATE
		
		return snapshot.energyLevel - (decreasePerCycle * elapsedCycles.toFloat())
	}
	
	private fun hasMovedTooFar(nbt: TagCompound, currentWorld: World, currentPos: BlockPos): Boolean {
		if (currentWorld.dimensionKey != nbt.getDecodedOrNull(INITIAL_DIMENSION_TAG, World.CODEC, HEE.log)) {
			return true
		}
		
		if (currentWorld.isEndDimension && TerritoryInstance.fromPos(currentPos) != nbt.getIntegerOrNull(INITIAL_TERRITORY_TAG)?.let(TerritoryInstance.Companion::fromHash)) {
			return true
		}
		
		return false
	}
	
	private fun shouldLoseHealth(cluster: TileEntityEnergyCluster, nbt: TagCompound, infusions: InfusionList): Boolean {
		if (infusions.has(SAFETY)) {
			return false
		}
		
		if (hasMovedTooFar(nbt, cluster.world!!, cluster.pos)) {
			return true
		}
		
		val totalEnergyLost = Internal(nbt.getInt(INITIAL_LEVEL_TAG)) - cluster.energyLevel
		
		if (totalEnergyLost > maxOf(Floating(1F), cluster.energyBaseCapacity * 0.2F)) {
			return true
		}
		
		return false
	}
}
