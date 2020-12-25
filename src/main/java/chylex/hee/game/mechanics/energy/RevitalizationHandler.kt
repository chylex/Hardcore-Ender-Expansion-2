package chylex.hee.game.mechanics.energy

import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Internal
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.world.breakBlock
import chylex.hee.game.world.component1
import chylex.hee.game.world.component2
import chylex.hee.game.world.component3
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModItems
import chylex.hee.system.delegate.NotifyOnChange
import chylex.hee.system.math.floorToInt
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.random.nextFloat
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.use
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.INBTSerializable
import kotlin.math.pow
import kotlin.math.sqrt

class RevitalizationHandler(private val cluster: TileEntityEnergyCluster) : INBTSerializable<TagCompound> {
	private companion object {
		private const val SUBSTANCE_TAG = "Substance"
		private const val LAST_DRAIN_TIME_TAG = "LastDrainTime"
		private const val DRAIN_AMOUNT_TAG = "DrainAmount"
		private const val DRAIN_TARGET_TAG = "DrainTarget"
		
		private const val DRAIN_RATE_TICKS = 19
	}
	
	private val isRevitalizing
		get() = lastDrainTime > 0L
	
	private var substance by NotifyOnChange<Byte>(0) { newValue ->
		cluster.clientOrbitingOrbs = newValue
	}
	
	private var lastDrainTime = 0L
	private var drainAmount = Internal(0)
	private var drainTarget = Units(0)
	private var ignoreDisturbance = false
	
	fun tick() {
		if (!isRevitalizing) {
			return
		}
		
		val currentTime = cluster.wrld.totalTime
		val timeDiff = currentTime - lastDrainTime
		
		if (timeDiff < DRAIN_RATE_TICKS) {
			return
		}
		
		lastDrainTime = currentTime
		
		for(cycle in 1..(timeDiff / DRAIN_RATE_TICKS)) {
			if (cluster.energyLevel <= drainTarget) {
				cluster.revitalizeHealth()
				
				substance = 0
				lastDrainTime = 0L
				drainAmount = Internal(0)
				drainTarget = Units(0)
				break
			}
			
			drain()
		}
	}
	
	private fun drain() {
		ignoreDisturbance = true
		val result = cluster.drainEnergy(drainAmount)
		ignoreDisturbance = false
		
		if (!result) {
			disturb()
		}
	}
	
	fun disturb(): Boolean {
		if (ignoreDisturbance) {
			return true
		}
		
		if (isRevitalizing) {
			cluster.pos.breakBlock(cluster.wrld, false)
			return false
		}
		else if (substance > 0) {
			val world = cluster.wrld
			val rand = world.rand
			
			val (x, y, z) = cluster.pos
			
			EntityItem(world, x + rand.nextFloat(0.25, 0.75), y + rand.nextFloat(0.25, 0.75), z + rand.nextFloat(0.25, 0.75), ItemStack(ModItems.REVITALIZATION_SUBSTANCE, substance.toInt())).apply {
				setDefaultPickupDelay()
				world.addEntity(this)
			}
			
			substance = 0
		}
		
		return true
	}
	
	fun addSubstance(): Boolean {
		val capacityUnits = cluster.energyBaseCapacity.units.value.toFloat()
		
		if (!isRevitalizing && ++substance >= ((capacityUnits + 8F) / 8F).pow(0.42F).floorToInt()) {
			val durationTicks = 20F * (50F + (30F * sqrt(capacityUnits))) / (0.25F + (0.75F * cluster.currentHealth.regenAmountMp))
			val toDrain = cluster.energyBaseCapacity * 0.5F
			
			lastDrainTime = cluster.wrld.totalTime
			drainAmount = maxOf(Internal(1), toDrain * (DRAIN_RATE_TICKS / durationTicks)).internal
			drainTarget = maxOf(Units(0), cluster.energyLevel - toDrain).units
			return true
		}
		
		return false
	}
	
	override fun serializeNBT() = TagCompound().apply {
		if (isRevitalizing) {
			putLong(LAST_DRAIN_TIME_TAG, lastDrainTime)
			putInt(DRAIN_AMOUNT_TAG, drainAmount.value)
			putInt(DRAIN_TARGET_TAG, drainTarget.value)
		}
		
		if (substance > 0) {
			putByte(SUBSTANCE_TAG, substance)
		}
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		substance = getByte(SUBSTANCE_TAG)
		lastDrainTime = getLong(LAST_DRAIN_TIME_TAG)
		drainAmount = Internal(getInt(DRAIN_AMOUNT_TAG))
		drainTarget = Units(getInt(DRAIN_TARGET_TAG))
	}
}
