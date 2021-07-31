package chylex.hee.game.item

import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.entity.util.REACH_DISTANCE
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.fx.util.ParticleSetting
import chylex.hee.game.fx.util.ParticleSetting.ALL
import chylex.hee.game.fx.util.ParticleSetting.DECREASED
import chylex.hee.game.fx.util.ParticleSetting.MINIMAL
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
import chylex.hee.util.buffer.use
import chylex.hee.util.buffer.writePos
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.math.floorToInt
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getPos
import chylex.hee.util.nbt.hasKey
import chylex.hee.util.nbt.putPos
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand.OFF_HAND
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import org.apache.commons.lang3.math.Fraction
import java.util.Random
import kotlin.math.max
import kotlin.math.pow

abstract class ItemAbstractEnergyUser(properties: Properties) : HeeItem(properties) {
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
			override fun write(buffer: PacketBuffer) = buffer.use {
				writePos(cluster.pos)
				writeInt(player.entityId)
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
			
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
				val cluster = readPos().getTile<TileEntityEnergyCluster>(world) ?: return
				val player = world.getEntityByID(readInt()) as? PlayerEntity ?: return
				
				ParticleSpawnerCustom(
					type = ParticleEnergyTransferToPlayer,
					data = ParticleEnergyTransferToPlayer.Data(cluster, player, 0.2),
					pos = InBox(0.01F + (0.08F * (cluster.energyLevel.floating.value / 40F).coerceAtMost(1F))),
					skipTest = ::particleSkipTest
				).spawn(Point(cluster.pos, 1), rand)
				
				// TODO sound
			}
		}
	}
	
	override val localizationExtra
		get() = mapOf(
			LANG_TOOLTIP_ENERGY_LEVEL to "§9Energy: §3%s§9 / §3%s",
			LANG_TOOLTIP_ENERGY_USES to "§9Uses Left: §3%s",
		)
	
	init {
		@Suppress("DEPRECATION")
		require(maxStackSize == 1) { "energy item must have a maximum stack size of 1" }
	}
	
	protected abstract fun getEnergyCapacity(stack: ItemStack): Units
	protected abstract fun getEnergyPerUse(stack: ItemStack): Fraction
	
	// Energy storage
	
	private fun calculateInternalEnergyCapacity(stack: ItemStack): Int {
		return getEnergyCapacity(stack).value * getEnergyPerUse(stack).denominator
	}
	
	private fun getEnergyLevel(stack: ItemStack): Int {
		return stack.heeTagOrNull?.getShort(ENERGY_LEVEL_TAG)?.toInt() ?: 0
	}
	
	private fun offsetEnergyLevel(stack: ItemStack, byAmount: Int): Boolean {
		with(stack.heeTag) {
			val prevLevel = getShort(ENERGY_LEVEL_TAG)
			val newLevel = (prevLevel + byAmount).coerceIn(0, calculateInternalEnergyCapacity(stack)).toShort()
			
			putShort(ENERGY_LEVEL_TAG, newLevel)
			return prevLevel != newLevel
		}
	}
	
	// Energy handling
	
	fun hasAnyEnergy(stack: ItemStack): Boolean {
		return getEnergyLevel(stack) > 0
	}
	
	fun hasMaximumEnergy(stack: ItemStack): Boolean {
		return getEnergyLevel(stack) >= calculateInternalEnergyCapacity(stack)
	}
	
	open fun chargeEnergyUnit(stack: ItemStack): Boolean {
		return offsetEnergyLevel(stack, getEnergyPerUse(stack).denominator)
	}
	
	open fun useEnergyUnit(stack: ItemStack): Boolean {
		return offsetEnergyLevel(stack, -getEnergyPerUse(stack).numerator) // TODO add FX when all Energy is used
	}
	
	fun useEnergyUnit(entity: Entity, stack: ItemStack): Boolean {
		return (entity is PlayerEntity && entity.abilities.isCreativeMode) || useEnergyUnit(stack)
	}
	
	fun getEnergyChargeLevel(stack: ItemStack): IEnergyQuantity {
		return Units(getEnergyLevel(stack) / getEnergyPerUse(stack).denominator)
	}
	
	fun setEnergyChargeLevel(stack: ItemStack, level: IEnergyQuantity) {
		val internalCapacity = calculateInternalEnergyCapacity(stack)
		stack.heeTag.putShort(ENERGY_LEVEL_TAG, (level.units.value * getEnergyPerUse(stack).denominator).coerceIn(0, internalCapacity).toShort())
	}
	
	fun setEnergyChargePercentage(stack: ItemStack, percentage: Float) {
		val internalCapacity = calculateInternalEnergyCapacity(stack)
		stack.heeTag.putShort(ENERGY_LEVEL_TAG, (internalCapacity * percentage).floorToInt().coerceIn(0, internalCapacity).toShort())
	}
	
	// Energy charging
	
	override fun onItemUse(context: ItemUseContext): ActionResultType {
		val player = context.player ?: return FAIL
		val world = context.world
		val pos = context.pos
		
		val tile = pos.getTile<TileEntityEnergyCluster>(world)
		val stack = player.getHeldItem(context.hand)
		
		if (tile == null || !player.canPlayerEdit(pos, context.face, stack)) {
			return FAIL
		}
		else if (world.isRemote) {
			return SUCCESS
		}
		
		with(stack.heeTag) {
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
	
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean) {
		if (world.isRemote || entity !is PlayerEntity) {
			return
		}
		
		with(stack.heeTagOrNull ?: return) {
			if (hasKey(CLUSTER_POS_TAG) && (world.gameTime + getByte(CLUSTER_TICK_OFFSET_TAG)) % 3L == 0L) {
				val pos = getPos(CLUSTER_POS_TAG)
				val tile = pos.getTile<TileEntityEnergyCluster>(world)
				
				if ((isSelected || entity.getHeldItem(OFF_HAND) === stack) &&
				    getShort(ENERGY_LEVEL_TAG) < calculateInternalEnergyCapacity(stack) &&
				    pos.distanceTo(entity) <= entity.getAttributeValue(REACH_DISTANCE) &&
				    tile != null &&
				    tile.drainEnergy(Units(1))
				) {
					chargeEnergyUnit(stack)
					PacketClientFX(FX_CHARGE, FxChargeData(tile, entity)).sendToAllAround(tile, 32.0)
				}
				else {
					removeClusterTags(this)
				}
			}
		}
	}
	
	// Client visuals
	
	override fun showDurabilityBar(stack: ItemStack) = true
	
	override fun getDurabilityForDisplay(stack: ItemStack) = 1.0 - (getEnergyLevel(stack).toDouble() / calculateInternalEnergyCapacity(stack))
	
	override fun getRGBDurabilityForDisplay(stack: ItemStack): Int {
		val level = 1F - getDurabilityForDisplay(stack).pow(2.0).toFloat()
		return MathHelper.hsvToRGB(max(0F, 0.62F + 0.15F * level), 0.45F + 0.15F * level, 0.8F + 0.2F * level)
	}
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean {
		return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag) {
		super.addInformation(stack, world, lines, flags)
		
		if (flags.isAdvanced) {
			lines.add(TranslationTextComponent(LANG_TOOLTIP_ENERGY_LEVEL, getEnergyLevel(stack), calculateInternalEnergyCapacity(stack)))
		}
		
		lines.add(TranslationTextComponent(LANG_TOOLTIP_ENERGY_USES, (getEnergyLevel(stack).toDouble() / getEnergyPerUse(stack).numerator).ceilToInt()))
	}
}
