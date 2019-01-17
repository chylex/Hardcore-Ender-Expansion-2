package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockEnergyCluster
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.game.mechanics.energy.ClusterColor
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
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.energy.ProximityHandler
import chylex.hee.game.particle.ParticleEnergyCluster
import chylex.hee.game.particle.spawner.IParticleSpawner
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.system.util.FLAG_SKIP_RENDER
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.allInCenteredBox
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.isAir
import chylex.hee.system.util.isAnyPlayerWithinRange
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos
import kotlin.math.max
import kotlin.math.pow

class TileEntityEnergyCluster : TileEntityBase(), ITickable{
	private companion object{
		private const val DEFAULT_NOTIFY_FLAGS = FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER or FLAG_MARK_DIRTY
		
		private const val REGEN_TICKS_TAG = "TicksToRegen"
		private const val SNAPSHOT_TAG = "Snapshot"
		private const val INACTIVE_TAG = "Inactive"
		private const val PROXIMITY_TAG = "Proximity"
	}
	
	enum class LeakType(val regenDelayTicks: Int, val corruptedEnergyDistance: Int){
		PROXIMITY(regenDelayTicks = 15, corruptedEnergyDistance = 4),
		INSTABILITY(regenDelayTicks = 200, corruptedEnergyDistance = 12),
	}
	
	// Properties (State)
	
	var energyLevel: IEnergyQuantity by Notifying(Internal(0), DEFAULT_NOTIFY_FLAGS)
		private set
	
	var energyBaseCapacity: IEnergyQuantity by Notifying(Internal(0), DEFAULT_NOTIFY_FLAGS)
		private set
	
	private var internalHealthStatus: HealthStatus by Notifying(HEALTHY, DEFAULT_NOTIFY_FLAGS)
	private var internalHealthOverride: HealthOverride? by Notifying(null, DEFAULT_NOTIFY_FLAGS)
	
	var color: ClusterColor by Notifying(ClusterColor(0, 0), DEFAULT_NOTIFY_FLAGS)
		private set
	
	// Properties (Calculated)
	
	val currentHealth: IClusterHealth
		get() = internalHealthOverride ?: internalHealthStatus
	
	val energyRegenCapacity: IEnergyQuantity
		get() = minOf(energyBaseCapacity * currentHealth.regenCapacityMp, MAX_REGEN_CAPACITY)
	
	val baseLeakSize: IEnergyQuantity
		get() = Floating(world.rand.nextFloat(0.5F, 1F) * ((1F + energyBaseCapacity.floating.value).pow(0.12F) + energyLevel.floating.value.pow(0.05F) - 2F))
	
	val affectedByProximity: Boolean
		get() = proximityHandler.affectedByProximity
	
	val wasUsedRecently: Boolean
		get() = world.totalWorldTime - lastUseTick < 20L
	
	// Variables
	
	private var ticksToRegen = 20
	private var lastUseTick = 0L
	private var isInactive = false
	
	private val proximityHandler = ProximityHandler(this)
	
	private var particle: Pair<IParticleSpawner, IShape>? = null
	private val particleSkipTest = ParticleEnergyCluster.newCountingSkipTest()
	
	var breakWithoutExplosion = false
	
	// Interactions
	
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
		
		lastUseTick = world.totalWorldTime
		isInactive = false
		return true
	}
	
	fun leakEnergy(quantity: IEnergyQuantity, type: LeakType){
		val finalQuantity = minOf(energyLevel, quantity)
		
		if (finalQuantity <= Units(0)){
			return
		}
		
		energyLevel -= finalQuantity
		ticksToRegen = max(ticksToRegen, type.regenDelayTicks)
		
		findLeakPos(type.corruptedEnergyDistance)?.let {
			BlockEnergyCluster.createSmallLeak(world, it, finalQuantity)
		}
	}
	
	fun deteriorateCapacity(corruptionLevel: Int){
		energyBaseCapacity -= maxOf(energyBaseCapacity * world.rand.nextFloat(0.025F, 0.035F), Units(corruptionLevel))
		
		if (energyLevel > energyRegenCapacity){
			energyLevel = energyRegenCapacity
		}
	}
	
	fun deteriorateHealth(): Boolean{
		return currentHealth.deterioratesTo?.let { internalHealthStatus = it; true } ?: false
	}
	
	private fun findLeakPos(maxDistance: Int): BlockPos?{
		val rand = world.rand
		
		repeat(25 * maxDistance){
			val testPos = pos.add(
				rand.nextInt(-maxDistance, maxDistance),
				rand.nextInt(-maxDistance, maxDistance),
				rand.nextInt(-maxDistance, maxDistance)
			)
			
			if (testPos.isAir(world)){
				return testPos
			}
		}
		
		return pos.allInCenteredBox(1, 1, 1).shuffled().firstOrNull { it.isAir(world) }
	}
	
	// Snapshot
	
	fun getClusterSnapshot(): ClusterSnapshot = ClusterSnapshot(
		energyLevel    = this.energyLevel,
		energyCapacity = this.energyBaseCapacity,
		healthStatus   = this.internalHealthStatus,
		healthOverride = this.internalHealthOverride,
		color          = this.color
	)
	
	fun loadClusterSnapshot(snapshot: ClusterSnapshot){
		energyLevel = snapshot.energyLevel
		energyBaseCapacity = snapshot.energyCapacity
		internalHealthStatus = snapshot.healthStatus
		internalHealthOverride = snapshot.healthOverride
		color = snapshot.color
		
		ticksToRegen = 40
		proximityHandler.reset()
	}
	
	fun setInactive(){
		isInactive = true
	}
	
	// Behavior
	
	override fun update(){
		if (world.isRemote){
			if (world.totalWorldTime % 3L == 0L){
				particle?.let { it.first.spawn(it.second, world.rand) }
			}
			
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
		
		if (currentHealth.affectedByProximity){
			proximityHandler.tick()
		}
		else{
			proximityHandler.reset()
		}
	}
	
	// Serialization
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		setShort(REGEN_TICKS_TAG, ticksToRegen.toShort())
		setTag(SNAPSHOT_TAG, getClusterSnapshot().tag)
		
		if (context == STORAGE){
			if (isInactive){
				setBoolean(INACTIVE_TAG, true)
			}
			else{
				setTag(PROXIMITY_TAG, proximityHandler.serializeNBT())
			}
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		ticksToRegen = getShort(REGEN_TICKS_TAG).toInt()
		loadClusterSnapshot(ClusterSnapshot(nbt.getCompoundTag(SNAPSHOT_TAG)))
		
		if (context == STORAGE){
			if (getBoolean(INACTIVE_TAG)){
				setInactive()
			}
			else{
				proximityHandler.deserializeNBT(getCompoundTag(PROXIMITY_TAG))
			}
		}
		else if (context == NETWORK){
			val particleSpawner = ParticleSpawnerCustom(
				type = ParticleEnergyCluster,
				data = ParticleEnergyCluster.Data(this@TileEntityEnergyCluster),
				pos = InBox(0.003F),
				mot = InBox(0.0015F),
				skipTest = particleSkipTest
			)
			
			if (particle == null){
				particleSpawner.spawn(Point(pos, 3), world.rand) // small quick burst when placed down
			}
			
			particle = Pair(particleSpawner, Point(pos, 1))
		}
	}
	
	// Rendering
	
	override fun hasFastRenderer(): Boolean = true
	override fun canRenderBreaking(): Boolean = false
}
