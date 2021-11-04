package chylex.hee.game.entity.living

import chylex.hee.HEE
import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.Resource
import chylex.hee.game.entity.living.ai.AITargetEyeContact
import chylex.hee.game.entity.living.ai.AIToggle
import chylex.hee.game.entity.living.ai.AIToggle.Companion.addGoal
import chylex.hee.game.entity.living.ai.AIWatchTargetInShock
import chylex.hee.game.entity.living.ai.AttackMelee
import chylex.hee.game.entity.living.ai.PickUpBlock
import chylex.hee.game.entity.living.ai.Swim
import chylex.hee.game.entity.living.ai.TargetEyeContact
import chylex.hee.game.entity.living.ai.WanderLand
import chylex.hee.game.entity.living.ai.WatchIdle
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler
import chylex.hee.game.entity.living.behavior.EndermanTeleportHandler
import chylex.hee.game.entity.living.behavior.EndermanWaterHandler
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.entity.properties.EntitySpawnPlacement
import chylex.hee.game.entity.technical.EntityTechnicalCausatumEvent
import chylex.hee.game.entity.util.DefaultEntityAttributes
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectAllEntities
import chylex.hee.game.entity.util.selectEntities
import chylex.hee.game.entity.util.with
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.mechanics.causatum.CausatumStage
import chylex.hee.game.mechanics.causatum.CausatumStage.S0_INITIAL
import chylex.hee.game.mechanics.causatum.EnderCausatum
import chylex.hee.game.mechanics.causatum.events.CausatumEventEndermanKill
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.system.heeTag
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.math.square
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityClassification.MONSTER
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MobEntity
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE
import net.minecraft.entity.ai.attributes.Attributes.FOLLOW_RANGE
import net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH
import net.minecraft.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.AbstractArrowEntity
import net.minecraft.entity.projectile.LlamaSpitEntity
import net.minecraft.entity.projectile.PotionEntity
import net.minecraft.entity.projectile.ThrowableEntity
import net.minecraft.network.datasync.DataParameter
import net.minecraft.util.DamageSource
import net.minecraft.util.IndirectEntityDamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.EntityRayTraceResult
import net.minecraft.world.Difficulty
import net.minecraft.world.IWorld
import net.minecraft.world.IWorldReader
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.World
import net.minecraft.world.biome.Biome.Category
import net.minecraft.world.biome.MobSpawnInfo
import net.minecraft.world.gen.Heightmap
import net.minecraftforge.event.entity.ProjectileImpactEvent
import net.minecraftforge.event.world.BiomeLoadingEvent
import java.util.Random
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

class EntityMobEnderman(type: EntityType<EntityMobEnderman>, world: World) : EntityMobAbstractEnderman(type, world) {
	constructor(world: World) : this(ModEntities.ENDERMAN, world)
	
	object Type : BaseType<EntityMobEnderman>() {
		override val localization
			get() = LocalizationStrategy.None
		
		override val attributes
			get() = DefaultEntityAttributes.hostileMob.with(
				MAX_HEALTH     to 40.0,
				ATTACK_DAMAGE  to 5.0,
				MOVEMENT_SPEED to 0.3,
				FOLLOW_RANGE   to 64.0,
			)
		
		override val placement: EntitySpawnPlacement<EntityMobEnderman>
			get() = EntitySpawnPlacement(PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Companion::canSpawnAt)
	}
	
	@SubscribeAllEvents(modid = HEE.ID)
	companion object {
		private const val TELEPORT_HANDLER_TAG = "Teleport"
		private const val WATER_HANDLER_TAG = "Water"
		private const val CAN_PICK_UP_BLOCKS_TAG = "CanPickUpBlocks"
		private const val HELD_BLOCK_TIMER_TAG = "HeldBlockTimer"
		private const val HELD_BLOCK_DESPAWNS_TAG = "HeldBlockDespawns"
		
		private val TELEPORT_AFTER_DAMAGE = setOf(
			DamageSource.ANVIL,
			DamageSource.CACTUS,
			DamageSource.CRAMMING,
			DamageSource.DROWN,
			DamageSource.FALLING_BLOCK,
			DamageSource.HOT_FLOOR,
			DamageSource.IN_FIRE,
			DamageSource.IN_WALL,
			DamageSource.LAVA,
			DamageSource.LIGHTNING_BOLT,
		)
		
		private val TELEPORT_RANGE_AVOID_BATTLE = (12.0)..(24.0)
		private val TELEPORT_RANGE_RANDOM_IDLE = (8.0)..(64.0)
		private val TELEPORT_RANGE_RANDOM_BUSY = (4.0)..(12.0)
		
		// Projectile event
		
		@SubscribeEvent
		fun onProjectileImpact(e: ProjectileImpactEvent) {
			val enderman = (e.rayTraceResult as? EntityRayTraceResult)?.entity
			
			if (enderman !is EntityMobEnderman) {
				return
			}
			
			if (enderman.world.isRemote) {
				e.isCanceled = true // stops arrows from bouncing off (and other projectiles from reacting to the canceled hit)
				return
			}
			
			val projectile = e.entity
			val tp = enderman.teleportHandler
			
			e.isCanceled = when (projectile) {
				is PotionEntity ->
					tp.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)
				
				is AbstractArrowEntity,
				is LlamaSpitEntity,
				is ThrowableEntity,
				is EntityProjectileSpatialDash ->
					tp.teleportDodge(projectile)
				
				else ->
					tp.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)
			}
			
			enderman.revengeTarget = when (projectile) {
				is ThrowableEntity     -> projectile.shooter as? LivingEntity
				is AbstractArrowEntity -> projectile.shooter as? LivingEntity
				else                   -> return
			}
		}
		
		// Biome spawns
		
		private fun isBiomeBlacklisted(biomeCategory: Category): Boolean {
			return biomeCategory == Category.NETHER || biomeCategory == Category.THEEND
		}
		
		private fun isEndermanEntry(entry: MobSpawnInfo.Spawners): Boolean {
			return entry.type == EntityType.ENDERMAN
		}
		
		@SubscribeEvent(priority = EventPriority.NORMAL)
		fun onBiomeLoading(e: BiomeLoadingEvent) {
			val spawns = e.spawns.getSpawner(MONSTER)
			
			if (isBiomeBlacklisted(e.category)) {
				spawns.removeAll(::isEndermanEntry)
			}
			else {
				val totalWeight = spawns.sumOf { it.itemWeight }                              // default 515
				val endermanWeight = spawns.filter(::isEndermanEntry).sumOf { it.itemWeight } // default 10
				
				if (endermanWeight > 0) {
					val newSingleWeight = (2 * endermanWeight) + (totalWeight / 20) // should be about 45 for most biomes
					val newGroupWeight = (2 * endermanWeight) / 3                   // should be about 4 for most biomes
					
					spawns.removeAll(::isEndermanEntry)
					spawns.add(MobSpawnInfo.Spawners(ModEntities.ENDERMAN, newSingleWeight, 1, 1))
					spawns.add(MobSpawnInfo.Spawners(ModEntities.ENDERMAN, newGroupWeight, 2, 3))
				}
			}
		}
		
		// Spawn conditions
		
		fun canSpawnAt(type: EntityType<out MobEntity>, world: IWorld, reason: SpawnReason, pos: BlockPos, rand: Random): Boolean {
			return world.difficulty != Difficulty.PEACEFUL && checkSpawnLightLevel(world, pos, rand) && canSpawnOn(type, world, reason, pos, rand)
		}
		
		private fun checkSpawnLightLevel(world: IWorld, pos: BlockPos, rand: Random): Boolean {
			if (world is World && world.isRainingAt(pos)) {
				return false
			}
			
			if (world.getLightFor(BLOCK, pos) >= rand.nextInt(4, 8)) {
				return false
			}
			
			return !world.dimensionType.isNatural || checkSkylightLevel(world, rand)
		}
		
		private fun checkSkylightLevel(world: IWorld, rand: Random): Boolean {
			val skylight = world.skylightSubtracted // goes from 0 (day) to 11 (night)
			val endermen = world.selectAllEntities.count { it is EntityMobEnderman }
			
			val extra = min(5, world.players.count { !it.isSpectator } - 1) // TODO use chunk set for better precision
			
			return (skylight >= 11 && endermen <= 40 + (extra * 5)) || (skylight >= rand.nextInt(3, 9) && endermen < (skylight * 2) + (extra * 2))
		}
	}
	
	// Instance
	
	private lateinit var teleportHandler: EndermanTeleportHandler
	private lateinit var waterHandler: EndermanWaterHandler
	private lateinit var blockHandler: EndermanBlockHandler
	
	private lateinit var aiAttackTarget: AIToggle
	private lateinit var aiPickUpBlocks: AIToggle
	private lateinit var aiWatchTargetInShock: AIWatchTargetInShock
	
	private var heldBlockTimer: Short = 0
	private var heldBlockDespawns = false
	
	private var trackedCausatumStage: CausatumStage? = null
	private var teleportAfterStare = false
	private var teleportAfterStareDelayedChance = 0F
	private var wasFirstKill = false
	
	override val teleportCooldown
		get() = when (trackedCausatumStage) {
			null       -> rand.nextInt(20 * 5, 20 * 10)
			S0_INITIAL -> rand.nextInt(20 * 7, 20 *  8)
			else       -> rand.nextInt(20 * 4, 20 *  5)
		}
	
	// Initialization
	
	init {
		experienceValue = 10
	}
	
	override fun registerGoals() {
		teleportHandler = EndermanTeleportHandler(this)
		waterHandler = EndermanWaterHandler(this, takeDamageAfterWetTicks = 80)
		blockHandler = EndermanBlockHandler(this)
		
		aiWatchTargetInShock = AIWatchTargetInShock(this, maxDistance = 72.0)
		
		aiAttackTarget = AIToggle()
		aiAttackTarget.enabled = true
		
		aiPickUpBlocks = AIToggle()
		aiPickUpBlocks.enabled = rand.nextInt(5) == 0
		
		goalSelector.addGoal(1, Swim(this))
		goalSelector.addGoal(2, aiWatchTargetInShock)
		goalSelector.addGoal(3, AttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = false), aiAttackTarget)
		goalSelector.addGoal(4, WanderLand(this, movementSpeed = 0.9, chancePerTick = 70))
		goalSelector.addGoal(5, WatchIdle(this))
		goalSelector.addGoal(6, PickUpBlock(this, ticksPerAttempt = 20 * 30, handler = blockHandler), aiPickUpBlocks)
		
		targetSelector.addGoal(1, TargetEyeContact(this, fieldOfView = 140F, headRadius = 0.19F, minStareTicks = 9, easilyReachableOnly = false, targetPredicate = ::canTriggerEyeContact))
	}
	
	// Behavior
	
	override fun livingTick() {
		super.livingTick()
		
		if (!world.isRemote) {
			teleportHandler.update()
			waterHandler.update()
			
			if (heldBlockTimer > 0 && --heldBlockTimer == 0.toShort()) {
				if (heldBlockDespawns || !blockHandler.tryPlaceBlock(allowPlayerProximity = false)) {
					teleportHandler.teleportOutOfWorld(force = rand.nextBoolean())
				}
			}
			
			if (idleTime > 150 && ticksExisted % 15 == 0) {
				var despawnChance = 300
				despawnChance -= 15 * (11 - world.skylightSubtracted) // skylightSubtracted goes from 0 (day) to 11 (night)
				despawnChance -= if (heldBlockState != null) 75 else 0
				despawnChance /= if (isWet) 8 else 1
				
				if (rand.nextInt(despawnChance) == 0) {
					teleportHandler.teleportOutOfWorld()
				}
			}
			
			if (attackTarget != null && !aiAttackTarget.enabled && !aiWatchTargetInShock.isWatching) {
				attackTarget = null
			}
			
			if (teleportAfterStare) {
				if (!aiWatchTargetInShock.isWatching) {
					teleportAfterStare = false
					
					if (rand.nextFloat() < teleportAfterStareDelayedChance) {
						teleportHandler.teleportDelayed(rand.nextInt(20, 50), ::teleportBehindTarget)
					}
					else {
						teleportBehindTarget()
					}
				}
			}
			else if (idleTime > 50 && teleportHandler.checkCooldownSilent() && rand.nextInt(300) == 0) {
				val xzDistance = if (attackTarget == null)
					TELEPORT_RANGE_RANDOM_IDLE
				else
					TELEPORT_RANGE_RANDOM_BUSY
				
				teleportHandler.teleportRandom(xzDistance)
			}
		}
	}
	
	override fun notifyDataManagerChange(key: DataParameter<*>) {
		super.notifyDataManagerChange(key)
		
		if (key === SCREAMING && world.isRemote && isAggro) {
			// TODO sound fx
		}
	}
	
	override fun setHeldBlockState(state: BlockState?) {
		super.setHeldBlockState(state)
		
		if (state == null) {
			heldBlockTimer = 0
		}
		else {
			val seconds = rand.nextInt(5, 45)
			
			heldBlockTimer = (20 * seconds).toShort()
			heldBlockDespawns = rand.nextFloat() > (seconds / 35F).pow(1.2F)
		}
	}
	
	// Battle (Interactions)
	
	private fun canTriggerEyeContact(target: PlayerEntity): Boolean {
		return when (EnderCausatum.getStage(target)) {
			S0_INITIAL -> getDistanceSq(target) <= square(32)
			else       -> true
		}
	}
	
	private fun beginStare(durationTicks: Int) {
		aiWatchTargetInShock.startWatching(durationTicks)
		aiWatchTargetInShock.tick() // forces look update for teleportation
		idleTime = 0
	}
	
	private fun beginDeathStare(delayedTeleportChance: Float) {
		beginStare(rand.nextInt(35, 55))
		teleportAfterStare = true
		teleportAfterStareDelayedChance = delayedTeleportChance
	}
	
	private fun beginScaredStare() {
		beginStare(rand.nextInt(120, 200))
	}
	
	private fun forceDropHeldBlock() {
		if (!blockHandler.tryPlaceBlock(allowPlayerProximity = true)) {
			blockHandler.dropBlockAsItem()
		}
	}
	
	private fun teleportBehindTarget(): Boolean {
		forceDropHeldBlock()
		return attackTarget?.let { teleportHandler.teleportAround(it, (-80F)..(80F), (-7.0)..(-2.0)) } ?: false
	}
	
	// Battle (Targeting)
	
	override fun setAttackTarget(newTarget: LivingEntity?) {
		if (attackTarget === newTarget) {
			return
		}
		
		if (newTarget == null) {
			super.setAttackTarget(null)
			trackedCausatumStage = null
			isAggro = false
		}
		else if (newTarget is PlayerEntity) {
			val currentStage = trackedCausatumStage
			val newTargetStage = EnderCausatum.getStage(newTarget)
			
			if (currentStage == null && newTargetStage == S0_INITIAL && teleportHandler.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)) {
				return
			}
			
			super.setAttackTarget(newTarget)
			
			trackedCausatumStage = maxOf(newTargetStage, currentStage ?: newTargetStage)
			
			if (trackedCausatumStage == S0_INITIAL) {
				aiAttackTarget.enabled = false
			}
			else {
				aiAttackTarget.enabled = true
				isAggro = true
			}
			
			if (targetSelector.runningGoals.anyMatch { it.goal is AITargetEyeContact<*> }) {
				if (trackedCausatumStage == S0_INITIAL) {
					beginScaredStare()
				}
				else {
					beginDeathStare(0.4F)
				}
			}
			
			heldBlockTimer = 0
		}
	}
	
	override fun setRevengeTarget(entity: LivingEntity?) {
		if (attackTarget == null && entity is PlayerEntity && rng.nextInt(4) == 0 && canEntityBeSeen(entity) && getDistanceSq(entity) < square(32)) {
			attackTarget = entity
			beginDeathStare(0.85F)
		}
	}
	
	// Battle (Damage)
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean {
		if (!world.isRemote) {
			val attacker = source.immediateSource as? PlayerEntity
			
			if (attacker != null) {
				if (trackedCausatumStage == S0_INITIAL || EnderCausatum.getStage(attacker) == S0_INITIAL) {
					trackedCausatumStage = S0_INITIAL // prevents teleport from setting attackTarget
					attackTarget = attacker
					beginScaredStare()
					forceDropHeldBlock()
					
					if (teleportHandler.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)) {
						return false
					}
				}
				else if (attackTarget == null) {
					attackTarget = attacker
					forceDropHeldBlock()
					
					if (teleportHandler.teleportDelayed(rand.nextInt(25, 55), ::teleportBehindTarget)) {
						return false
					}
				}
			}
		}
		
		if (!super.attackEntityFrom(source, amount)) {
			return false
		}
		
		if (trackedCausatumStage == S0_INITIAL) {
			beginScaredStare()
		}
		else {
			aiWatchTargetInShock.stopWatching()
		}
		
		if (TELEPORT_AFTER_DAMAGE.contains(source)) {
			if (health < rand.nextFloat(6F, 12F) || !teleportHandler.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)) {
				teleportHandler.teleportOutOfWorld()
			}
		}
		else if (source is IndirectEntityDamageSource) {
			teleportHandler.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)
			
			if (revengeTarget == null) {
				revengeTarget = source.trueSource as? LivingEntity
			}
		}
		
		return true
	}
	
	override fun onDeath(cause: DamageSource) {
		if (dead) {
			return
		}
		
		val attacker = cause.trueSource as? PlayerEntity ?: attackingPlayer
		
		if (attacker != null) {
			if (EnderCausatum.triggerStage(attacker, CausatumStage.S1_KILLED_ENDERMAN)) {
				// TODO achievement after the event finishes & compendium
				
				for (player in world.selectEntities.inRange<PlayerEntity>(posVec, 32.0)) {
					if (player !== attacker && abs(player.posY - posY) < 8.0) {
						EnderCausatum.triggerStage(player, CausatumStage.S1_KILLED_ENDERMAN)
					}
				}
				
				ModSounds.MOB_ENDERMAN_FIRST_KILL.playServer(world, posVec, SoundCategory.HOSTILE)
				
				EntityTechnicalCausatumEvent(world, CausatumEventEndermanKill(this, attacker)).apply {
					setPosition(this@EntityMobEnderman.posX, this@EntityMobEnderman.posY, this@EntityMobEnderman.posZ)
					world.addEntity(this)
				}
				
				wasFirstKill = true
			}
			else {
				// TODO achievement
			}
		}
		
		super.onDeath(cause)
	}
	
	// Spawning & despawning
	
	override fun getBlockPathWeight(pos: BlockPos, world: IWorldReader): Float {
		return 1F
	}
	
	override fun canDespawn(distanceToClosestPlayerSq: Double): Boolean {
		return super.canDespawn(distanceToClosestPlayerSq) && !teleportHandler.preventDespawn
	}
	
	// Properties
	
	override fun getLootTable(): ResourceLocation {
		return if (wasFirstKill)
			Resource.Custom("entities/enderman_first_kill")
		else
			Resource.Custom("entities/enderman")
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		put(TELEPORT_HANDLER_TAG, teleportHandler.serializeNBT())
		put(WATER_HANDLER_TAG, waterHandler.serializeNBT())
		
		putBoolean(CAN_PICK_UP_BLOCKS_TAG, aiPickUpBlocks.enabled)
		putShort(HELD_BLOCK_TIMER_TAG, heldBlockTimer)
		putBoolean(HELD_BLOCK_DESPAWNS_TAG, heldBlockDespawns)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		teleportHandler.deserializeNBT(getCompound(TELEPORT_HANDLER_TAG))
		waterHandler.deserializeNBT(getCompound(WATER_HANDLER_TAG))
		
		aiPickUpBlocks.enabled = getBoolean(CAN_PICK_UP_BLOCKS_TAG)
		heldBlockTimer = getShort(HELD_BLOCK_TIMER_TAG)
		heldBlockDespawns = getBoolean(HELD_BLOCK_DESPAWNS_TAG)
	}
}
