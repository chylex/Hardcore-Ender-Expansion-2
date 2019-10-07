package chylex.hee.game.mechanics.energy
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Internal
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.init.ModItems
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextFloat
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.INBTSerializable
import kotlin.math.pow
import kotlin.math.sqrt

class RevitalizationHandler(private val cluster: TileEntityEnergyCluster) : INBTSerializable<TagCompound>{
	private companion object{
		private const val SUBSTANCE_TAG = "Substance"
		private const val LAST_DRAIN_TIME_TAG = "LastDrainTime"
		private const val DRAIN_AMOUNT_TAG = "DrainAmount"
		private const val DRAIN_TARGET_TAG = "DrainTarget"
		
		private const val DRAIN_RATE_TICKS = 19
	}
	
	private val isRevitalizing
		get() = lastDrainTime > 0L
	
	private var substance by NotifyOnChange<Byte>(0){
		newValue -> cluster.clientOrbitingOrbs = newValue
	}
	
	private var lastDrainTime = 0L
	private var drainAmount = Internal(0)
	private var drainTarget = Units(0)
	private var ignoreDisturbance = false
	
	fun tick(){
		if (!isRevitalizing){
			return
		}
		
		val currentTime = cluster.world.totalWorldTime
		val timeDiff = currentTime - lastDrainTime
		
		if (timeDiff < DRAIN_RATE_TICKS){
			return
		}
		
		lastDrainTime = currentTime
		
		for(cycle in 1..(timeDiff / DRAIN_RATE_TICKS)){
			if (cluster.energyLevel <= drainTarget){
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
	
	private fun drain(){
		ignoreDisturbance = true
		val result = cluster.drainEnergy(drainAmount)
		ignoreDisturbance = false
		
		if (!result){
			disturb()
		}
	}
	
	fun disturb(): Boolean{
		if (ignoreDisturbance){
			return true
		}
		
		if (isRevitalizing){
			cluster.pos.breakBlock(cluster.world, false)
			return false
		}
		else if (substance > 0){
			val world = cluster.world
			val rand = world.rand
			
			val (x, y, z) = cluster.pos
			
			EntityItem(world, x + rand.nextFloat(0.25, 0.75), y + rand.nextFloat(0.25, 0.75), z + rand.nextFloat(0.25, 0.75), ItemStack(ModItems.REVITALIZATION_SUBSTANCE, substance.toInt())).apply {
				setDefaultPickupDelay()
				world.spawnEntity(this)
			}
			
			substance = 0
		}
		
		return true
	}
	
	fun addSubstance(): Boolean{
		val capacityUnits = cluster.energyBaseCapacity.units.value.toFloat()
		
		if (!isRevitalizing && ++substance >= ((capacityUnits + 8F) / 8F).pow(0.42F).floorToInt()){
			val durationTicks = 20F * (50F + (30F * sqrt(capacityUnits))) / (0.25F + (0.75F * cluster.currentHealth.regenAmountMp))
			val toDrain = cluster.energyBaseCapacity * 0.5F
			
			lastDrainTime = cluster.world.totalWorldTime
			drainAmount = maxOf(Internal(1), toDrain * (DRAIN_RATE_TICKS / durationTicks)).internal
			drainTarget = maxOf(Units(0), cluster.energyLevel - toDrain).units
			return true
		}
		
		return false
	}
	
	override fun serializeNBT() = TagCompound().apply {
		if (isRevitalizing){
			setLong(LAST_DRAIN_TIME_TAG, lastDrainTime)
			setInteger(DRAIN_AMOUNT_TAG, drainAmount.value)
			setInteger(DRAIN_TARGET_TAG, drainTarget.value)
		}
		
		if (substance > 0){
			setByte(SUBSTANCE_TAG, substance)
		}
	}
	
	override fun deserializeNBT(nbt: TagCompound) = with(nbt){
		substance = getByte(SUBSTANCE_TAG)
		lastDrainTime = getLong(LAST_DRAIN_TIME_TAG)
		drainAmount = Internal(getInteger(DRAIN_AMOUNT_TAG))
		drainTarget = Units(getInteger(DRAIN_TARGET_TAG))
	}
}
