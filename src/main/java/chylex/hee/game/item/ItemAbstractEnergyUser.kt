package chylex.hee.game.item

import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.entity.util.REACH_DISTANCE
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.fx.util.ParticleSetting
import chylex.hee.game.fx.util.ParticleSetting.ALL
import chylex.hee.game.fx.util.ParticleSetting.DECREASED
import chylex.hee.game.fx.util.ParticleSetting.MINIMAL
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.IItemDurabilityComponent
import chylex.hee.game.item.components.IReequipAnimationComponent
import chylex.hee.game.item.components.ITickInInventoryComponent
import chylex.hee.game.item.components.ITooltipComponent
import chylex.hee.game.item.components.PlayerUseItemOnBlockComponent
import chylex.hee.game.mechanics.energy.IEnergyItem
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.particle.ParticleEnergyTransferToPlayer
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.util.distanceTo
import chylex.hee.game.world.util.getTile
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.buffer.readPos
import chylex.hee.util.buffer.writePos
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.math.floorToInt
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getPos
import chylex.hee.util.nbt.hasKey
import chylex.hee.util.nbt.putPos
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand.OFF_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import java.util.Random
import kotlin.math.max
import kotlin.math.pow

class ItemAbstractEnergyUser(private val impl: IEnergyItem) : HeeItemBuilder() {
	companion object {
		private const val ENERGY_LEVEL_TAG = "EnergyLevel"
		
		private const val CLUSTER_POS_TAG = "ClusterPos"
		private const val CLUSTER_TICK_OFFSET_TAG = "ClusterTick"
		
		private const val LANG_TOOLTIP_ENERGY_LEVEL = "item.tooltip.hee.energy.level"
		private const val LANG_TOOLTIP_ENERGY_USES = "item.tooltip.hee.energy.uses"
		
		private fun removeClusterTags(nbt: TagCompound) {
			nbt.remove(CLUSTER_POS_TAG)
			nbt.remove(CLUSTER_TICK_OFFSET_TAG)
		}
		
		class FxChargeData(private val cluster: TileEntityEnergyCluster, private val player: PlayerEntity) : IFxData {
			override fun write(buffer: PacketBuffer) {
				buffer.writePos(cluster.pos)
				buffer.writeInt(player.entityId)
			}
		}
		
		val FX_CHARGE = object : IFxHandler<FxChargeData> {
			private var particleSkipCounter = 0
			
			@Suppress("UNUSED_PARAMETER")
			private fun particleSkipTest(distanceSq: Double, particleSetting: ParticleSetting, rand: Random): Boolean {
				return when (particleSetting) {
					ALL       -> false
					DECREASED -> ++particleSkipCounter % 2 != 0
					MINIMAL   -> ++particleSkipCounter % 3 != 0
				}
			}
			
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) {
				val cluster = buffer.readPos().getTile<TileEntityEnergyCluster>(world) ?: return
				val player = world.getEntityByID(buffer.readInt()) as? PlayerEntity ?: return
				
				ParticleSpawnerCustom(
					type = ParticleEnergyTransferToPlayer,
					data = ParticleEnergyTransferToPlayer.Data(cluster, player, 0.2),
					pos = InBox(0.01F + (0.08F * (cluster.energyLevel.floating.value / 40F).coerceAtMost(1F))),
					skipTest = ::particleSkipTest
				).spawn(Point(cluster.pos, 1), rand)
				
				// TODO sound
			}
		}
		
		// Energy storage
		
		private fun getEnergyLevel(stack: ItemStack): Int {
			return stack.heeTagOrNull?.getShort(ENERGY_LEVEL_TAG)?.toInt() ?: 0
		}
		
		private fun IEnergyItem.calculateInternalEnergyCapacity(stack: ItemStack): Int {
			return getEnergyCapacity(stack).value * getEnergyPerUse(stack).denominator
		}
		
		private fun IEnergyItem.offsetEnergyLevel(stack: ItemStack, byAmount: Int): Boolean {
			with(stack.heeTag) {
				val prevLevel = getShort(ENERGY_LEVEL_TAG)
				val newLevel = (prevLevel + byAmount).coerceIn(0, calculateInternalEnergyCapacity(stack)).toShort()
				
				putShort(ENERGY_LEVEL_TAG, newLevel)
				return prevLevel != newLevel
			}
		}
	}
	
	init {
		localizationExtra[LANG_TOOLTIP_ENERGY_LEVEL] = "§9Energy: §3%s§9 / §3%s"
		localizationExtra[LANG_TOOLTIP_ENERGY_USES] = "§9Uses Left: §3%s"
		
		components.tooltip.add(ITooltipComponent { lines, stack, advanced, _ ->
			val energy = getEnergyLevel(stack)
			
			if (advanced) {
				lines.add(TranslationTextComponent(LANG_TOOLTIP_ENERGY_LEVEL, energy, impl.calculateInternalEnergyCapacity(stack)))
			}
			
			lines.add(TranslationTextComponent(LANG_TOOLTIP_ENERGY_USES, (energy.toDouble() / impl.getEnergyPerUse(stack).numerator).ceilToInt()))
		})
		
		components.reequipAnimation = IReequipAnimationComponent.AnimateIfSlotChanged
		
		components.useOnBlock = object : PlayerUseItemOnBlockComponent() {
			override fun use(world: World, pos: BlockPos, player: PlayerEntity, heldItem: ItemStack, context: ItemUseContext): ActionResultType {
				if (pos.getTile<TileEntityEnergyCluster>(world) == null) {
					return FAIL
				}
				
				if (world.isRemote) {
					return SUCCESS
				}
				
				with(heldItem.heeTag) {
					if (hasKey(CLUSTER_POS_TAG)) {
						removeClusterTags(this)
					}
					else if (pos.distanceTo(player) <= player.getAttributeValue(REACH_DISTANCE)) {
						putPos(CLUSTER_POS_TAG, pos)
						putByte(CLUSTER_TICK_OFFSET_TAG, (4L - (world.gameTime % 4L)).toByte())
					}
				}
				
				return SUCCESS
			}
		}
		
		components.tickInInventory.add(object : ITickInInventoryComponent {
			override fun tick(world: World, entity: Entity, stack: ItemStack, slot: Int, isSelected: Boolean) {
				if (world.isRemote || entity !is PlayerEntity) {
					return
				}
				
				with(stack.heeTagOrNull ?: return) {
					if (hasKey(CLUSTER_POS_TAG) && (world.gameTime + getByte(CLUSTER_TICK_OFFSET_TAG)) % 3L == 0L) {
						val pos = getPos(CLUSTER_POS_TAG)
						val tile = pos.getTile<TileEntityEnergyCluster>(world)
						
						if ((isSelected || entity.getHeldItem(OFF_HAND) === stack) &&
							getShort(ENERGY_LEVEL_TAG) < impl.calculateInternalEnergyCapacity(stack) &&
							pos.distanceTo(entity) <= entity.getAttributeValue(REACH_DISTANCE) &&
							tile != null &&
							tile.drainEnergy(Units(1))
						) {
							impl.chargeUnit(stack)
							PacketClientFX(FX_CHARGE, FxChargeData(tile, entity)).sendToAllAround(tile, 32.0)
						}
						else {
							removeClusterTags(this)
						}
					}
				}
			}
		})
		
		components.durability = EnergyDurabilityComponent(impl)
		
		interfaces[IEnergyItem::class.java] = impl
		
		callbacks.add {
			@Suppress("DEPRECATION")
			require(maxStackSize == 1) { "energy item must have a maximum stack size of 1" }
		}
	}
	
	// Durability
	
	abstract class EnergyItem : IEnergyItem {
		final override fun hasAnyEnergy(stack: ItemStack): Boolean {
			return getEnergyLevel(stack) > 0
		}
		
		final override fun hasMaximumEnergy(stack: ItemStack): Boolean {
			return getEnergyLevel(stack) >= calculateInternalEnergyCapacity(stack)
		}
		
		final override fun chargeUnit(stack: ItemStack): Boolean {
			if (!offsetEnergyLevel(stack, getEnergyPerUse(stack).denominator)) {
				return false
			}
			
			onUnitCharged(stack)
			return true
		}
		
		final override fun useUnit(entity: Entity, stack: ItemStack): Boolean {
			if (entity is PlayerEntity && entity.abilities.isCreativeMode) {
				return true
			}
			
			return offsetEnergyLevel(stack, -getEnergyPerUse(stack).numerator) // TODO add FX when all Energy is used
		}
		
		final override fun getChargeLevel(stack: ItemStack): IEnergyQuantity {
			return Units(getEnergyLevel(stack) / getEnergyPerUse(stack).denominator)
		}
		
		final override fun setChargeLevel(stack: ItemStack, level: IEnergyQuantity) {
			val internalCapacity = calculateInternalEnergyCapacity(stack)
			stack.heeTag.putShort(ENERGY_LEVEL_TAG, (level.units.value * getEnergyPerUse(stack).denominator).coerceIn(0, internalCapacity).toShort())
		}
		
		final override fun setChargePercentage(stack: ItemStack, percentage: Float) {
			val internalCapacity = calculateInternalEnergyCapacity(stack)
			stack.heeTag.putShort(ENERGY_LEVEL_TAG, (internalCapacity * percentage).floorToInt().coerceIn(0, internalCapacity).toShort())
		}
		
		protected open fun onUnitCharged(stack: ItemStack) {}
	}
	
	open class EnergyDurabilityComponent(private val user: IEnergyItem) : IItemDurabilityComponent {
		override fun showBar(stack: ItemStack): Boolean {
			return true
		}
		
		override fun getDisplayDurability(stack: ItemStack): Double {
			return 1.0 - (getEnergyLevel(stack).toDouble() / user.calculateInternalEnergyCapacity(stack))
		}
		
		override fun getDisplayDurabilityRGB(stack: ItemStack): Int {
			val level = 1F - getDisplayDurability(stack).pow(2.0).toFloat()
			return MathHelper.hsvToRGB(max(0F, 0.62F + 0.15F * level), 0.45F + 0.15F * level, 0.8F + 0.2F * level)
		}
	}
}
