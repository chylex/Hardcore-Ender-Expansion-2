package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.game.mechanics.energy.ClusterSnapshot
import chylex.hee.game.mechanics.energy.IClusterHealth
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.DAMAGED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.HEALTHY
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.UNSTABLE
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.MAX_POSSIBLE_VALUE
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.MAX_REGEN_CAPACITY
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Floating
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Internal
import chylex.hee.system.util.FLAG_SKIP_RENDER
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.isAnyPlayerWithinRange
import chylex.hee.system.util.nextItem
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable
import kotlin.math.pow

class TileEntityEnergyCluster : TileEntityBase(), ITickable{
	private companion object{
		const val DEFAULT_NOTIFY_FLAGS = FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER
		
		const val SNAPSHOT_TAG = "Snapshot"
		const val INACTIVE_TAG = "Inactive"
	}
	
	// Properties (State)
	
	var energyLevel: IEnergyQuantity by Notifying(Internal(0), DEFAULT_NOTIFY_FLAGS)
		private set
	
	var energyBaseCapacity: IEnergyQuantity by Notifying(Internal(0), DEFAULT_NOTIFY_FLAGS)
		private set
	
	private var internalHealthStatus: HealthStatus by Notifying(HEALTHY, DEFAULT_NOTIFY_FLAGS)
	private var internalHealthOverride: HealthOverride? by Notifying(null, DEFAULT_NOTIFY_FLAGS)
	
	// Properties (Calculated)
	
	val currentHealth: IClusterHealth
		get() = internalHealthOverride ?: internalHealthStatus
	
	val energyRegenCapacity: IEnergyQuantity
		get() = minOf(energyBaseCapacity * currentHealth.regenCapacityMp, MAX_REGEN_CAPACITY)
	
	// Variables
	
	private var ticksToRegen = 20
	private var isInactive = false
	
	var breakWithoutExplosion = false
	
	// Methods
	
	fun drainEnergy(quantity: IEnergyQuantity): Boolean{
		if (energyLevel < quantity){
			return false
		}
		else if (energyLevel == MAX_POSSIBLE_VALUE){
			pos.breakBlock(world, false)
			return false
		}
		
		energyLevel -= quantity
		ticksToRegen = 20 + (40F / currentHealth.regenSpeedMp).ceilToInt()
		
		isInactive = false
		return true
	}
	
	fun deteriorateHealth(): Boolean{
		return currentHealth.deterioratesTo?.let { internalHealthStatus = it; true } ?: false
	}
	
	// Snapshot
	
	fun getClusterSnapshot(): ClusterSnapshot = ClusterSnapshot(
		energyLevel    = this.energyLevel,
		energyCapacity = this.energyBaseCapacity,
		healthStatus   = this.internalHealthStatus,
		healthOverride = this.internalHealthOverride
	)
	
	fun loadClusterSnapshot(data: ClusterSnapshot){
		energyLevel = data.energyLevel
		energyBaseCapacity = data.energyCapacity
		internalHealthStatus = data.healthStatus
		internalHealthOverride = data.healthOverride
		
		ticksToRegen = 40
	}
	
	// Overrides
	
	override fun update(){
		if (world.isRemote){
			return
		}
		
		if (isInactive){
			if (world.totalWorldTime % 80L == 0L){
				val activationRange = when(internalHealthStatus){
					UNSTABLE -> 48.0
					DAMAGED  -> 32.0
					else     -> 24.0
				}
				
				if (pos.isAnyPlayerWithinRange(world, activationRange)){
					isInactive = false
				}
			}
			
			return
		}
		
		if (energyLevel < energyRegenCapacity && --ticksToRegen < 0){
			energyLevel = minOf(energyRegenCapacity, energyLevel + Floating(((1 + energyBaseCapacity.floating.value).pow(0.004F) - 0.997F) * 0.5F) * currentHealth.regenAmountMp)
			ticksToRegen = (20F / currentHealth.regenSpeedMp).ceilToInt()
		}
	}
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		setTag(SNAPSHOT_TAG, getClusterSnapshot().tag)
		
		if (context == STORAGE && isInactive){
			setBoolean(INACTIVE_TAG, true)
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		loadClusterSnapshot(ClusterSnapshot(nbt.getCompoundTag(SNAPSHOT_TAG)))
		isInactive = getBoolean(INACTIVE_TAG)
	}
	
	override fun hasFastRenderer(): Boolean = true
	override fun canRenderBreaking(): Boolean = false
}
