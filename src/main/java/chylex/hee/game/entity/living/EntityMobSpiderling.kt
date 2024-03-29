package chylex.hee.game.entity.living

import chylex.hee.game.Resource
import chylex.hee.game.entity.IHeeMobEntityType
import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.entity.living.ai.AIAttackLeap
import chylex.hee.game.entity.living.ai.AIToggle
import chylex.hee.game.entity.living.ai.AIToggle.Companion.addGoal
import chylex.hee.game.entity.living.ai.AIWanderLightStartle.ILightStartleHandler
import chylex.hee.game.entity.living.ai.AttackLeap
import chylex.hee.game.entity.living.ai.AttackMelee
import chylex.hee.game.entity.living.ai.Swim
import chylex.hee.game.entity.living.ai.TargetAttacker
import chylex.hee.game.entity.living.ai.TargetNearby
import chylex.hee.game.entity.living.ai.WanderLandStopNear
import chylex.hee.game.entity.living.ai.WanderLightStartle
import chylex.hee.game.entity.living.ai.WanderOnFirePanic
import chylex.hee.game.entity.living.ai.WatchClosest
import chylex.hee.game.entity.living.ai.WatchIdle
import chylex.hee.game.entity.living.path.PathNavigateGroundUnrestricted
import chylex.hee.game.entity.properties.EntitySize
import chylex.hee.game.entity.properties.EntitySpawnPlacement
import chylex.hee.game.entity.util.DefaultEntityAttributes
import chylex.hee.game.entity.util.EntityData
import chylex.hee.game.entity.util.OP_MUL_INCR_INDIVIDUAL
import chylex.hee.game.entity.util.getAttributeInstance
import chylex.hee.game.entity.util.motionY
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectEntities
import chylex.hee.game.entity.util.selectExistingEntities
import chylex.hee.game.entity.util.selectVulnerableEntities
import chylex.hee.game.entity.util.tryApplyNonPersistentModifier
import chylex.hee.game.entity.util.tryRemoveModifier
import chylex.hee.game.entity.util.with
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.isPeaceful
import chylex.hee.init.ModEntities
import chylex.hee.system.heeTag
import chylex.hee.util.math.Vec
import chylex.hee.util.math.addY
import chylex.hee.util.math.directionTowards
import chylex.hee.util.math.square
import chylex.hee.util.math.toRadians
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import net.minecraft.block.BlockState
import net.minecraft.block.WebBlock
import net.minecraft.entity.CreatureAttribute
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.entity.ILivingEntityData
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.SpawnReason.SPAWNER
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE
import net.minecraft.entity.ai.attributes.Attributes.FOLLOW_RANGE
import net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH
import net.minecraft.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import net.minecraft.entity.monster.MonsterEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.AxeItem
import net.minecraft.item.SwordItem
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.pathfinding.PathNavigator
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.Effects
import net.minecraft.util.DamageSource
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceContext.BlockMode
import net.minecraft.util.math.RayTraceContext.FluidMode
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.Difficulty.HARD
import net.minecraft.world.Difficulty.NORMAL
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.IServerWorld
import net.minecraft.world.IWorldReader
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.LightType.SKY
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.fml.network.NetworkHooks
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sin

class EntityMobSpiderling(type: EntityType<EntityMobSpiderling>, world: World) : MonsterEntity(type, world), ILightStartleHandler {
	constructor(world: World) : this(ModEntities.SPIDERLING, world)
	
	object Type : IHeeMobEntityType<EntityMobSpiderling> {
		override val classification
			get() = EntityClassification.MONSTER
		
		override val size
			get() = EntitySize(0.675F, 0.45F)
		
		override val tracker
			get() = super.tracker.copy(updateInterval = 2)
		
		override val attributes
			get() = DefaultEntityAttributes.hostileMob.with(
				ATTACK_DAMAGE  to 1.5,
				MOVEMENT_SPEED to 0.32,
				FOLLOW_RANGE   to 20.0,
			)
		
		override val placement
			get() = EntitySpawnPlacement.hostile<EntityMobSpiderling>()
	}
	
	companion object {
		private val DAMAGE_GENERAL = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS)
		private val FALL_CRIT_DAMAGE = AttributeModifier("Fall crit damage", 0.5, OP_MUL_INCR_INDIVIDUAL)
		
		private val DATA_SLEEPING = EntityData.register<EntityMobSpiderling, Boolean>(DataSerializers.BOOLEAN)
		
		val LOOT_TABLE = Resource.Custom("entities/spiderling")
		
		private const val BASE_JUMP_MOTION = 0.38F
		
		private const val SLEEP_STATE_TAG = "SleepState"
		private const val LIGHT_STARTLE_RESET_TIME_TAG = "LightStartleResetTime"
		
		private fun findTopBlockMaxY(world: World, pos: BlockPos): Double? {
			var lastTop: Double? = null
			
			for (y in 0..3) {
				val testPos = pos.up(y)
				val testBox = testPos.getState(world).getCollisionShape(world, testPos)
				
				if (testBox.isEmpty) {
					return lastTop?.let { it + testPos.y - 1 }
				}
				else {
					lastTop = testBox.boundingBox.maxY
				}
			}
			
			return null
		}
	}
	
	// Instance
	
	var isSleepingProp by EntityData(DATA_SLEEPING)
		private set
	
	private var lightStartleResetTime = 0L
	
	private lateinit var aiMovement: AIToggle
	
	private var jumpCooldown = 0
	private var wakeUpTimer = 0
	private var wakeUpDelayAI = 0
	private var canSleepAgain = true
	private var isBeingSwept = false
	
	private val panicSearchWaterChance
		get() = if (attackTarget == null || health > maxHealth * 0.5F) 1F else 0.15F
	
	private val panicSearchLandChance
		get() = if (attackTarget == null) 1F else 0.25F
	
	// Initialization
	
	init {
		stepHeight = 0F
		experienceValue = 2
		
		getAttributeInstance(MAX_HEALTH).baseValue = rand.nextInt(11, 13).toDouble()
		health = maxHealth
	}
	
	override fun registerData() {
		super.registerData()
		dataManager.register(DATA_SLEEPING, true)
	}
	
	override fun registerGoals() {
		aiMovement = AIToggle()
		aiMovement.enabled = false
		
		goalSelector.addGoal(1, Swim(this))
		goalSelector.addGoal(2, WanderOnFirePanic(this, movementSpeed = 1.2, searchWaterChance = ::panicSearchWaterChance, maxWaterDistanceXZ = 20, maxLandDistanceY = 5, searchLandChance = ::panicSearchLandChance, maxLandDistanceXZ = 5, maxWaterDistanceY = 2))
		goalSelector.addGoal(3, WanderLightStartle(this, movementSpeed = 1.2, minBlockLightIncrease = 3, minCombinedLightDecrease = 6, searchAttempts = 1000, maxDistanceXZ = 15, maxDistanceY = 2, handler = this))
		goalSelector.addGoal(4, AttackLeap(this, triggerDistance = (1.5)..(3.5), triggerChance = 0.75F, triggerCooldown = 45, leapStrengthXZ = (0.9)..(1.1), leapStrengthY = (0.4)..(0.5)), aiMovement)
		goalSelector.addGoal(5, AttackMelee(this, movementSpeed = 1.1, chaseAfterLosingSight = true), aiMovement)
		goalSelector.addGoal(6, WanderLandStopNear<PlayerEntity>(this, movementSpeed = 0.9, chancePerTick = 30, detectDistance = 7.5), aiMovement)
		goalSelector.addGoal(7, WatchClosest<PlayerEntity>(this, maxDistance = 3.5F), aiMovement)
		goalSelector.addGoal(8, WatchIdle(this), aiMovement)
		
		targetSelector.addGoal(1, TargetAttacker(this, callReinforcements = false))
		targetSelector.addGoal(2, TargetNearby(this, chancePerTick = 18, checkSight = true, easilyReachableOnly = false, targetPredicate = ::isPlayerNearbyForAttack), aiMovement)
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	// Behavior (General)
	
	override fun livingTick() {
		super.livingTick()
		
		if (world.isRemote) {
			if (ticksExisted == 1) { // fix first tick body rotation delay
				rotationYaw = rotationYawHead
				setRenderYawOffset(rotationYawHead)
			}
			
			return
		}
		
		if (world.isPeaceful && attackTarget != null) {
			super.setAttackTarget(null)
			lightStartleResetTime = 0L
		}
		
		if (wakeUpDelayAI > 0) {
			if (--wakeUpDelayAI == 0) {
				aiMovement.enabled = true
			}
		}
		else if (wakeUpTimer > 0) {
			if (--wakeUpTimer == 0) {
				wakeUp(preventSleep = false)
			}
		}
		else if (ticksExisted % 4 == 0) {
			if (isSleeping) {
				if (wakeUpTimer == 0) {
					val nearestPlayer = findPlayerInSight(maxDistance = 8.0, maxDiffY = 3)
					
					if (nearestPlayer != null) {
						onDisturbed()
					}
				}
			}
			else if (canSleepAgain && navigator.noPath() && rand.nextInt(6) == 0) {
				if (findPlayerInSight(maxDistance = 16.0, maxDiffY = 6) == null) {
					isSleepingProp = true
					aiMovement.enabled = false
				}
			}
		}
		
		if (onGround) {
			getAttributeInstance(ATTACK_DAMAGE).tryRemoveModifier(FALL_CRIT_DAMAGE)
		}
		
		if (jumpCooldown > 0) {
			--jumpCooldown
		}
		
		if (jumpCooldown == 0 && onGround && ticksExisted % 3 == 0) {
			if (navigator.noPath()) {
				jumpCooldown = 10
			}
			else {
				val start = posVec.addY(0.5)
				val target = Vec(moveHelper.x, start.y, moveHelper.z)
				
				val direction = start.directionTowards(target)
				val obstacle = world.rayTraceBlocks(RayTraceContext(start, start.add(direction.scale(3.0)), BlockMode.COLLIDER, FluidMode.NONE, this))
				
				if (obstacle.type == RayTraceResult.Type.BLOCK) {
					val topY = findTopBlockMaxY(world, obstacle.pos)
					
					if (topY != null) {
						val diffY = (topY - posY).coerceIn(0.5, 2.5)
						val jumpMotion = (0.514 * log10(diffY + 0.35)) + 0.416 // goes roughly from 0.38 for half a block, to 0.65 for two and a half blocks
						
						if (!collidedHorizontally) {
							val strafeMotionMp = 0.2 + (jumpMotion - BASE_JUMP_MOTION)
							motion = motion.add(direction.x * strafeMotionMp, 0.0, direction.z * strafeMotionMp)
						}
						
						motionY = jumpMotion
						isAirBorne = true
						ForgeHooks.onLivingJump(this)
						
						jumpCooldown = rand.nextInt(18, 22)
						getAttributeInstance(ATTACK_DAMAGE).tryApplyNonPersistentModifier(FALL_CRIT_DAMAGE)
					}
				}
			}
		}
	}
	
	private fun onDisturbed() {
		if (!isSleeping || wakeUpTimer != 0) {
			return
		}
		
		wakeUpTimer = 5 * rand.nextInt(1, 12)
		
		for (nearbySpiderling in world.selectExistingEntities.inRange<EntityMobSpiderling>(posVec, 3.5)) {
			if (nearbySpiderling !== this) {
				nearbySpiderling.onDisturbed()
			}
		}
	}
	
	fun wakeUpInstantly(preventSleep: Boolean) {
		wakeUp(preventSleep, aiDelayTicks = 1)
	}
	
	fun wakeUp(preventSleep: Boolean) {
		wakeUp(preventSleep, aiDelayTicks = rand.nextInt(25, 40))
	}
	
	fun wakeUp(preventSleep: Boolean, aiDelayTicks: Int) {
		if (isSleeping) {
			isSleepingProp = false
			wakeUpDelayAI = aiDelayTicks
			
			if (preventSleep) {
				canSleepAgain = false
			}
		}
	}
	
	override fun setFire(seconds: Int) {
		wakeUpInstantly(preventSleep = true)
		super.setFire(seconds)
	}
	
	override fun isPotionApplicable(effect: EffectInstance): Boolean {
		return effect.potion != Effects.POISON && super.isPotionApplicable(effect)
	}
	
	// Behavior (Light)
	
	override fun onLightStartled(): Boolean {
		wakeUp(preventSleep = true)
		
		if (world.gameTime < lightStartleResetTime) {
			return false
		}
		
		lightStartleResetTime = 0L
		
		if (attackTarget == null) {
			attackTarget = world.getClosestPlayer(this@EntityMobSpiderling, 10.0)
		}
		
		return true
	}
	
	override fun onDarknessReached() {
		val peaceChance = when (world.difficulty) {
			HARD   -> 0.25F
			NORMAL -> 0.75F
			else   -> 1F
		}
		
		if (rand.nextFloat() < peaceChance) {
			attackTarget = null
		}
	}
	
	// Targeting
	
	private fun isPlayerNearbyForAttack(player: PlayerEntity): Boolean {
		return getDistanceSq(player) < square(1.75)
	}
	
	private fun findPlayerInSight(maxDistance: Double, maxDiffY: Int): PlayerEntity? {
		return world.selectVulnerableEntities.inRange<PlayerEntity>(posVec, maxDistance).firstOrNull {
			abs(posY - it.posY).roundToInt() <= maxDiffY && canEntityBeSeen(it)
		}
	}
	
	override fun setAttackTarget(newTarget: LivingEntity?) {
		if (!world.isPeaceful && newTarget !== attackTarget) {
			super.setAttackTarget(newTarget)
			
			if (attackTarget == null && lightStartleResetTime == 0L) {
				lightStartleResetTime = world.gameTime + (30L * 20L)
			}
		}
	}
	
	// Movement
	
	override fun createNavigator(world: World): PathNavigator {
		return PathNavigateGroundUnrestricted(this, world)
	}
	
	override fun getBlockPathWeight(pos: BlockPos, world: IWorldReader): Float {
		return 25F - world.getLightFor(BLOCK, pos) - (world.getLightFor(SKY, pos) * 0.5F)
	}
	
	override fun jump() { // POLISH could improve by making the motion less smooth when starting the jump, somehow
		if (jumpCooldown == 0) {
			getAttributeInstance(ATTACK_DAMAGE).tryApplyNonPersistentModifier(FALL_CRIT_DAMAGE)
			super.jump()
		}
	}
	
	override fun getJumpUpwardsMotion(): Float {
		return BASE_JUMP_MOTION
	}
	
	override fun getMaxFallHeight(): Int {
		return 256
	}
	
	override fun setMotionMultiplier(state: BlockState, mp: Vector3d) {
		if (state.block !is WebBlock) {
			super.setMotionMultiplier(state, mp)
		}
	}
	
	// Damage
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean {
		if (source === DamageSource.FALL) {
			return false
		}
		
		wakeUpInstantly(preventSleep = true)
		
		if (!super.attackEntityFrom(source, if (source.isFireDamage) amount * 1.25F else amount)) {
			return false
		}
		
		val player = source.trueSource as? PlayerEntity
		
		if (!isBeingSwept && player != null && player.getHeldItem(MAIN_HAND).item.let { it is SwordItem || it is AxeItem }) {
			val yaw = player.rotationYaw.toDouble().toRadians()
			val xRatio = sin(yaw)
			val zRatio = -cos(yaw)
			
			for (nearby in world.selectEntities.inRange<EntityMobSpiderling>(posVec, 2.0)) {
				if (nearby !== this) {
					nearby.isBeingSwept = true
					nearby.applyKnockback(0.4F, xRatio, zRatio)
					nearby.attackEntityFrom(DamageSource.causePlayerDamage(player), amount * rand.nextFloat(0.75F, 1F))
					nearby.isBeingSwept = false
				}
			}
		}
		
		return true
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean {
		if (goalSelector.runningGoals.anyMatch { it.goal is AIAttackLeap }) {
			getAttributeInstance(ATTACK_DAMAGE).tryApplyNonPersistentModifier(FALL_CRIT_DAMAGE)
		}
		
		if (!DAMAGE_GENERAL.dealToFrom(entity, this)) {
			return false
		}
		
		canSleepAgain = false
		
		if (isBurning && world.difficulty >= NORMAL) {
			entity.setFire(3)
		}
		
		return true
	}
	
	// Spawning
	
	override fun onInitialSpawn(world: IServerWorld, difficulty: DifficultyInstance, reason: SpawnReason, data: ILivingEntityData?, nbt: CompoundNBT?): ILivingEntityData? {
		if (reason == SPAWNER) {
			wakeUpInstantly(preventSleep = true)
		}
		
		return super.onInitialSpawn(world, difficulty, reason, data, nbt)
	}
	
	// Despawning
	
	override fun isDespawnPeaceful(): Boolean {
		return false
	}
	
	// Properties
	
	override fun getLootTable(): ResourceLocation {
		return LOOT_TABLE
	}
	
	override fun getExperiencePoints(player: PlayerEntity): Int {
		return rand.nextInt(0, experienceValue)
	}
	
	override fun getCreatureAttribute(): CreatureAttribute {
		return CreatureAttribute.ARTHROPOD
	}
	
	override fun isSleeping(): Boolean {
		return isSleepingProp
	}
	
	override fun playAmbientSound() {
		if (!isSleeping && rand.nextInt(5) <= 1) {
			super.playAmbientSound()
		}
	}
	
	override fun playStepSound(pos: BlockPos, state: BlockState) {
		playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.15F, soundPitch)
	}
	
	public override fun getAmbientSound(): SoundEvent {
		return SoundEvents.ENTITY_SPIDER_AMBIENT
	}
	
	public override fun getHurtSound(source: DamageSource): SoundEvent {
		return SoundEvents.ENTITY_SPIDER_HURT
	}
	
	public override fun getDeathSound(): SoundEvent {
		return SoundEvents.ENTITY_SPIDER_DEATH
	}
	
	public override fun getSoundPitch(): Float {
		return rand.nextFloat(1.2F, 1.5F)
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		putInt(SLEEP_STATE_TAG, when {
			isSleeping    -> 2
			canSleepAgain -> 1
			else          -> 0
		})
		
		putLong(LIGHT_STARTLE_RESET_TIME_TAG, lightStartleResetTime)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		val sleepState = getInt(SLEEP_STATE_TAG)
		
		if (sleepState != 2) {
			wakeUpInstantly(preventSleep = sleepState != 1)
		}
		
		lightStartleResetTime = getLong(LIGHT_STARTLE_RESET_TIME_TAG)
	}
}
