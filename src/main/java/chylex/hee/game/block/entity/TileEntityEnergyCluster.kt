package chylex.hee.game.block.entity

import chylex.hee.game.block.BlockEnergyCluster
import chylex.hee.game.block.entity.base.TileEntityBase
import chylex.hee.game.block.entity.base.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.mechanics.energy.ClusterColor
import chylex.hee.game.mechanics.energy.ClusterSnapshot
import chylex.hee.game.mechanics.energy.IClusterHealth
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride.REVITALIZING
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
import chylex.hee.game.mechanics.energy.RevitalizationHandler
import chylex.hee.game.particle.ParticleEnergyCluster
import chylex.hee.game.particle.ParticleEnergyClusterRevitalization
import chylex.hee.game.particle.base.ParticleBaseEnergy.ClusterParticleDataGenerator
import chylex.hee.game.particle.spawner.IParticleSpawner
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.util.FLAG_SKIP_RENDER
import chylex.hee.game.world.util.FLAG_SYNC_CLIENT
import chylex.hee.game.world.util.allInCenteredBox
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.isAir
import chylex.hee.game.world.util.isAnyPlayerWithinRange
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModTileEntities
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.BlockPos
import kotlin.math.max
import kotlin.math.pow

class TileEntityEnergyCluster(type: TileEntityType<TileEntityEnergyCluster>) : TileEntityBase(type), ITickableTileEntity {
	constructor() : this(ModTileEntities.ENERGY_CLUSTER)
	
	object Type : IHeeTileEntityType<TileEntityEnergyCluster> {
		override val blocks
			get() = arrayOf(ModBlocks.ENERGY_CLUSTER)
	}
	
	private companion object {
		private const val DEFAULT_NOTIFY_FLAGS = FLAG_SYNC_CLIENT or FLAG_SKIP_RENDER or FLAG_MARK_DIRTY
		
		private const val REGEN_TICKS_TAG = "TicksToRegen"
		private const val SNAPSHOT_TAG = "Snapshot"
		private const val INACTIVE_TAG = "Inactive"
		private const val PROXIMITY_TAG = "Proximity"
		private const val REVITALIZATION_TAG = "Revitalization"
		private const val ORBITING_ORBS_TAG = "OrbitingOrbs"
		
		private val PARTICLE_ORBITING = ParticleSpawnerCustom(
			type = ParticleEnergyClusterRevitalization,
			skipTest = { _, _, _ -> false }
		)
	}
	
	enum class LeakType(val regenDelayTicks: Int, val corruptedEnergyDistance: Int, val causesInstability: Boolean) {
		HEALTH(regenDelayTicks = 25, corruptedEnergyDistance = 4, causesInstability = true),
		PROXIMITY(regenDelayTicks = 15, corruptedEnergyDistance = 4, causesInstability = false),
		INSTABILITY(regenDelayTicks = 200, corruptedEnergyDistance = 12, causesInstability = false),
	}
	
	// Properties (State)
	
	var energyLevel: IEnergyQuantity by Notifying(Internal(0), DEFAULT_NOTIFY_FLAGS)
		private set
	
	var energyBaseCapacity: IEnergyQuantity by Notifying(Internal(0), DEFAULT_NOTIFY_FLAGS)
		private set
	
	private var internalHealthStatus: HealthStatus by Notifying(HEALTHY, DEFAULT_NOTIFY_FLAGS)
	private var internalHealthOverride: HealthOverride? by Notifying(null, DEFAULT_NOTIFY_FLAGS)
	
	var color by Notifying(ClusterColor(0, 0), DEFAULT_NOTIFY_FLAGS)
		private set
	
	var clientOrbitingOrbs: Byte by Notifying(0, DEFAULT_NOTIFY_FLAGS)
	
	// Properties (Calculated)
	
	val currentHealth: IClusterHealth
		get() = internalHealthOverride ?: internalHealthStatus
	
	val energyRegenCapacity: IEnergyQuantity
		get() = minOf(energyBaseCapacity * currentHealth.regenCapacityMp, MAX_REGEN_CAPACITY)
	
	val baseLeakSize: IEnergyQuantity
		get() = Floating(wrld.rand.nextFloat(0.4F, 0.8F) * ((1F + energyBaseCapacity.floating.value).pow(0.09F) + energyLevel.floating.value.pow(0.05F) - 2.1F))
	
	val affectedByProximity
		get() = proximityHandler.affectedByProximity
	
	val wasUsedRecently
		get() = wrld.gameTime - lastUseTick < 20L
	
	// Fields
	
	private var ticksToRegen = 20
	private var lastUseTick = 0L
	private var isInactive = false
	
	private val proximityHandler = ProximityHandler(this)
	private val revitalizationHandler = RevitalizationHandler(this)
	
	private var particle: Pair<IParticleSpawner, IShape>? = null
	private val particleSkipTest = ParticleEnergyCluster.newCountingSkipTest()
	
	var particleDataGenerator: ClusterParticleDataGenerator? = null
		private set
	
	private var particleOrbitingTimer = 0
	
	var breakWithoutExplosion = false
	
	// Interactions
	
	fun tryDisturb(): Boolean {
		return !breakIfDemonic() && revitalizationHandler.disturb()
	}
	
	fun drainEnergy(quantity: IEnergyQuantity): Boolean {
		if (breakIfDemonic() || energyLevel < quantity) {
			return false
		}
		
		energyLevel -= quantity
		ticksToRegen = 20 + (40F / currentHealth.regenSpeedMp).ceilToInt()
		
		lastUseTick = wrld.gameTime
		isInactive = false
		
		return tryDisturb()
	}
	
	fun leakEnergy(quantity: IEnergyQuantity, type: LeakType) {
		val finalQuantity = minOf(energyLevel, quantity)
		
		if (finalQuantity < Units(1)) {
			return
		}
		
		energyLevel -= finalQuantity
		ticksToRegen = max(ticksToRegen, type.regenDelayTicks)
		
		findLeakPos(type.corruptedEnergyDistance)?.let {
			BlockEnergyCluster.createSmallLeak(wrld, it, finalQuantity, type.causesInstability)
		}
	}
	
	fun deteriorateCapacity(corruptionLevel: Int) {
		energyBaseCapacity -= maxOf(energyBaseCapacity * wrld.rand.nextFloat(0.025F, 0.035F), Units(corruptionLevel))
		
		if (energyLevel > energyRegenCapacity) {
			energyLevel = energyRegenCapacity
		}
	}
	
	fun deteriorateHealth(): Boolean {
		return currentHealth.deterioratesTo?.let { internalHealthStatus = it; true } ?: false
	}
	
	fun addRevitalizationSubstance(): Boolean {
		if (breakIfDemonic() || internalHealthStatus.revitalizesTo == null || internalHealthOverride != null || energyLevel < energyBaseCapacity * 0.55F) {
			return false
		}
		
		if (revitalizationHandler.addSubstance()) {
			internalHealthOverride = REVITALIZING
		}
		
		return true
	}
	
	fun revitalizeHealth() {
		if (currentHealth != REVITALIZING) {
			return
		}
		
		internalHealthOverride = null
		internalHealthStatus.revitalizesTo?.let { internalHealthStatus = it }
	}
	
	// Helpers
	
	private fun breakIfDemonic(): Boolean {
		if (energyLevel == MAX_POSSIBLE_VALUE) {
			pos.breakBlock(wrld, false)
			return true
		}
		
		return false
	}
	
	private fun findLeakPos(maxDistance: Int): BlockPos? {
		val rand = wrld.rand
		
		repeat(25 * maxDistance) {
			val testPos = pos.add(
				rand.nextInt(-maxDistance, maxDistance),
				rand.nextInt(-maxDistance, maxDistance),
				rand.nextInt(-maxDistance, maxDistance)
			)
			
			if (testPos.isAir(wrld)) {
				return testPos
			}
		}
		
		return pos.allInCenteredBox(1, 1, 1).toList().shuffled(rand).firstOrNull { it.isAir(wrld) }
	}
	
	// Snapshot
	
	fun getClusterSnapshot() = ClusterSnapshot(
		energyLevel    = this.energyLevel,
		energyCapacity = this.energyBaseCapacity,
		healthStatus   = this.internalHealthStatus,
		healthOverride = this.internalHealthOverride,
		color          = this.color
	)
	
	fun loadClusterSnapshot(snapshot: ClusterSnapshot, inactive: Boolean) {
		energyLevel = snapshot.energyLevel
		energyBaseCapacity = snapshot.energyCapacity
		internalHealthStatus = snapshot.healthStatus
		internalHealthOverride = snapshot.healthOverride
		color = snapshot.color
		
		ticksToRegen = 40
		isInactive = inactive
		proximityHandler.reset()
	}
	
	// Behavior
	
	override fun tick() {
		if (wrld.isRemote) {
			if (wrld.gameTime % 3L == 0L) {
				particle?.let { it.first.spawn(it.second, wrld.rand) }
			}
			
			if (clientOrbitingOrbs > 0 && ++particleOrbitingTimer > (ParticleEnergyClusterRevitalization.TOTAL_LIFESPAN / clientOrbitingOrbs)) {
				particleOrbitingTimer = 0
				PARTICLE_ORBITING.spawn(Point(pos, 1), wrld.rand)
			}
			
			return
		}
		
		if (isInactive) {
			if (wrld.gameTime % 80L == 0L) {
				val activationRange = when (internalHealthStatus) {
					UNSTABLE -> 48.0
					DAMAGED  -> 32.0
					else     -> 24.0
				}
				
				if (pos.isAnyPlayerWithinRange(wrld, activationRange)) {
					isInactive = false
				}
			}
			
			return
		}
		
		if (energyLevel < energyRegenCapacity && --ticksToRegen < 0) {
			val amountMp = currentHealth.regenAmountMp
			
			if (amountMp > 0F) {
				energyLevel = minOf(energyRegenCapacity, energyLevel + Floating(((1 + energyBaseCapacity.floating.value).pow(0.004F) - 0.997F) * 0.5F) * amountMp)
			}
			
			ticksToRegen = (20F / currentHealth.regenSpeedMp).ceilToInt()
		}
		
		if (currentHealth.getLeakChance(this).let { it > 0F && wrld.rand.nextFloat() < it }) {
			leakEnergy(baseLeakSize, LeakType.HEALTH)
		}
		
		if (currentHealth.affectedByProximity) {
			proximityHandler.tick()
		}
		else {
			proximityHandler.reset()
		}
		
		revitalizationHandler.tick()
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		putShort(REGEN_TICKS_TAG, ticksToRegen.toShort())
		put(SNAPSHOT_TAG, getClusterSnapshot().tag)
		
		if (context == STORAGE) {
			if (isInactive) {
				putBoolean(INACTIVE_TAG, true)
			}
			else {
				put(PROXIMITY_TAG, proximityHandler.serializeNBT())
				put(REVITALIZATION_TAG, revitalizationHandler.serializeNBT())
			}
		}
		else if (context == NETWORK) {
			if (clientOrbitingOrbs > 0) {
				putByte(ORBITING_ORBS_TAG, clientOrbitingOrbs)
			}
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		ticksToRegen = getShort(REGEN_TICKS_TAG).toInt()
		loadClusterSnapshot(ClusterSnapshot(nbt.getCompound(SNAPSHOT_TAG)), isInactive)
		
		if (context == STORAGE) {
			if (getBoolean(INACTIVE_TAG)) {
				isInactive = true
			}
			else {
				proximityHandler.deserializeNBT(getCompound(PROXIMITY_TAG))
				revitalizationHandler.deserializeNBT(getCompound(REVITALIZATION_TAG))
			}
		}
		else if (context == NETWORK) {
			val prevOrbitingOrbs = clientOrbitingOrbs
			clientOrbitingOrbs = getByte(ORBITING_ORBS_TAG)
			
			if (clientOrbitingOrbs > prevOrbitingOrbs) {
				particleOrbitingTimer = 0
				PARTICLE_ORBITING.spawn(Point(pos, 1), wrld.rand)
			}
			
			val particleDataGen = ClusterParticleDataGenerator(this@TileEntityEnergyCluster).also {
				particleDataGenerator = it
			}
			
			val particleSpawner = ParticleSpawnerCustom(
				type = ParticleEnergyCluster,
				data = particleDataGen,
				pos = InBox(0.003F),
				mot = InBox(0.0015F),
				skipTest = particleSkipTest
			)
			
			if (particle == null) {
				particleSpawner.spawn(Point(pos, 3), wrld.rand) // small quick burst when placed down
			}
			
			particle = Pair(particleSpawner, Point(pos, 1))
		}
	}
}
