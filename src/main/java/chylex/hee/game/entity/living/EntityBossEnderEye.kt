package chylex.hee.game.entity.living

import chylex.hee.game.Resource
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.IHeeMobEntityType
import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.FallbackDamage
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ARMOR_PROTECTION
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ENCHANTMENT_PROTECTION
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.FIRE_TYPE
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.NUDITY_DANGER
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.RAPID_DAMAGE
import chylex.hee.game.entity.living.behavior.EnderEyeAttack.KnockbackDash
import chylex.hee.game.entity.living.behavior.EnderEyeAttack.Melee
import chylex.hee.game.entity.living.behavior.EnderEyePhase
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Attacking
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Floating
import chylex.hee.game.entity.living.behavior.EnderEyePhase.OpenEye
import chylex.hee.game.entity.living.behavior.EnderEyePhase.SleepingPhase
import chylex.hee.game.entity.living.behavior.EnderEyePhase.SleepingPhase.Hibernated
import chylex.hee.game.entity.living.behavior.EnderEyePhase.SleepingPhase.Sleeping
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Spawners
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Staring
import chylex.hee.game.entity.living.behavior.EnderEyeSpawnerParticles
import chylex.hee.game.entity.living.controller.EntityBodyHeadOnly
import chylex.hee.game.entity.living.controller.EntityLookSlerp
import chylex.hee.game.entity.living.controller.EntityMoveFlyingForward
import chylex.hee.game.entity.living.path.PathNavigateFlyingPreferBeeLineOrStrafe
import chylex.hee.game.entity.properties.EntitySize
import chylex.hee.game.entity.properties.EntityTrackerInfo
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.OBSIDIAN_TOWER_DEATH_ANIMATION
import chylex.hee.game.entity.util.DefaultEntityAttributes
import chylex.hee.game.entity.util.EntityData
import chylex.hee.game.entity.util.getAttributeInstance
import chylex.hee.game.entity.util.motionY
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectVulnerableEntities
import chylex.hee.game.entity.util.with
import chylex.hee.game.potion.BanishmentEffect
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientLaunchInstantly
import chylex.hee.system.heeTag
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItemOrNull
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.LerpedFloat
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.center
import chylex.hee.util.math.directionTowards
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.scale
import chylex.hee.util.math.square
import chylex.hee.util.math.toYaw
import chylex.hee.util.math.withY
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getPosOrNull
import chylex.hee.util.nbt.putPos
import chylex.hee.util.nbt.use
import net.minecraft.block.material.PushReaction
import net.minecraft.entity.CreatureAttribute
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.entity.FlyingEntity
import net.minecraft.entity.ILivingEntityData
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.Pose
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE
import net.minecraft.entity.ai.attributes.Attributes.FLYING_SPEED
import net.minecraft.entity.ai.attributes.Attributes.FOLLOW_RANGE
import net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH
import net.minecraft.entity.ai.controller.BodyController
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.pathfinding.PathNavigator
import net.minecraft.util.DamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.BossInfo
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.IServerWorld
import net.minecraft.world.World
import net.minecraft.world.server.ServerBossInfo
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.fml.network.NetworkHooks
import kotlin.math.abs
import kotlin.math.min

class EntityBossEnderEye(type: EntityType<EntityBossEnderEye>, world: World) : FlyingEntity(type, world), IMob, IKnockbackMultiplier {
	constructor(world: World) : this(ModEntities.ENDER_EYE, world)
	
	constructor(world: World, totalSpawners: Int) : this(world) {
		this.totalSpawners = totalSpawners.toShort()
		this.rotationYaw = 0F
	}
	
	object Type : IHeeMobEntityType<EntityBossEnderEye> {
		override val classification
			get() = EntityClassification.MONSTER
		
		override val size
			get() = EntitySize(1.1F, 1F)
		
		override val tracker
			get() = EntityTrackerInfo(trackingRange = 160, updateInterval = 1, receiveVelocityUpdates = true)
		
		override val attributes
			get() = DefaultEntityAttributes.hostileMob.with(
				MAX_HEALTH    to 300.0,
				ATTACK_DAMAGE to 4.0,
				FLYING_SPEED  to 0.093,
				FOLLOW_RANGE  to 16.0,
			)
		
		override val isImmuneToFire
			get() = true
	}
	
	companion object {
		val DAMAGE_MELEE = Damage(DIFFICULTY_SCALING, ARMOR_PROTECTION(false), ENCHANTMENT_PROTECTION)
		val DAMAGE_DASH = Damage(DIFFICULTY_SCALING, ARMOR_PROTECTION(false))
		val DAMAGE_LASER = FallbackDamage(
			Damage(FIRE_TYPE(5), DIFFICULTY_SCALING, *ALL_PROTECTIONS, NUDITY_DANGER, RAPID_DAMAGE(2)) to 1.0,
			Damage(DIFFICULTY_SCALING, *ALL_PROTECTIONS, NUDITY_DANGER, RAPID_DAMAGE(2)) to 0.5
		)
		
		private val DATA_SLEEPING = EntityData.register<EntityBossEnderEye, Boolean>(DataSerializers.BOOLEAN)
		private val DATA_DEMON_LEVEL = EntityData.register<EntityBossEnderEye, Byte>(DataSerializers.BYTE)
		private val DATA_EYE_STATE = EntityData.register<EntityBossEnderEye, Byte>(DataSerializers.BYTE)
		private val DATA_ARM_POSITION = EntityData.register<EntityBossEnderEye, Byte>(DataSerializers.BYTE)
		private val DATA_ROTATE_TARGET_ID = EntityData.register<EntityBossEnderEye, Int>(DataSerializers.VARINT)
		private val DATA_ROTATE_TARGET_SPEED = EntityData.register<EntityBossEnderEye, Float>(DataSerializers.FLOAT)
		
		private const val DEFAULT_SLERP_ADJUSTMENT_SPEED = 0.5F
		
		private val DEMON_LEVEL_DMG = arrayOf(1.0F, 1.15F, 1.3F, 1.5F, 1.75F, 2.0F)
		private val DEMON_LEVEL_XP  = arrayOf(1.0F, 1.2F,  1.5F, 1.8F, 2.0F,  2.2F)
		
		private const val KNOCKBACK_MP = 0.15
		
		private const val TOWER_CENTER_POS_TAG = "TowerCenter"
		private const val TOTAL_SPAWNERS_TAG = "TotalSpawners"
		private const val SPAWNER_PARTICLES_TAG = "SpawnerParticles"
		private const val REAL_MAX_HEALTH_TAG = "RealMaxHealth"
		private const val SLEEPING_TAG = "Sleeping"
		private const val DEMON_LEVEL_TAG = "DemonLevel"
		private const val PHASE_TAG = "Phase"
		private const val PHASE_DATA_TAG = "PhaseData"
		
		const val DEMON_EYE_LEVEL = 99.toByte()
		
		const val EYE_CLOSED: Byte = 0
		const val EYE_OPEN: Byte = 1
		const val EYE_LASER: Byte = 2
		
		const val ARMS_LIMP: Byte = 0
		const val ARMS_HUG: Byte = 1
		const val ARMS_ATTACK: Byte = 2
		
		const val LASER_DISTANCE = 32.0
	}
	
	// Instance
	
	private val bossInfo = ServerBossInfo(displayName, BossInfo.Color.PINK, BossInfo.Overlay.PROGRESS).apply { isVisible = false }
	
	var totalSpawners: Short = 0
		private set
	
	var realMaxHealth = 0F
	
	var isSleepingClientProp by EntityData(DATA_SLEEPING)
		private set
	
	var demonLevel by EntityData(DATA_DEMON_LEVEL)
		private set
	
	val isDemonEye
		get() = demonLevel == DEMON_EYE_LEVEL
	
	var eyeState by EntityData(DATA_EYE_STATE)
	var armPosition by EntityData(DATA_ARM_POSITION)
	val clientArmAngle = LerpedFloat(0F)
	
	private var rotateTargetId by EntityData(DATA_ROTATE_TARGET_ID)
	private var rotateTargetSpeed by EntityData(DATA_ROTATE_TARGET_SPEED)
	
	val spawnerParticles = EnderEyeSpawnerParticles(this)
	private var towerCenterPos: BlockPos? = null
	
	private val damageMultiplier
		get() = if (isDemonEye) 2.5F else DEMON_LEVEL_DMG.getOrElse(demonLevel.toInt()) { 1F }
	
	private val experienceMultiplier
		get() = if (isDemonEye) 6F else DEMON_LEVEL_XP.getOrElse(demonLevel.toInt()) { 1F }
	
	override var lastHitKnockbackMultiplier = 1F
	
	private val slerpLookController: EntityLookSlerp
	private var bossPhase: EnderEyePhase = Hibernated
	private var fallAsleepTimer = 0
	private var knockbackDashChance = 5 // slightly higher chance of knockback after fight (re)starts
	private var lastKnockbackDashTime = 0L
	
	init {
		moveController = EntityMoveFlyingForward(this)
		slerpLookController = EntityLookSlerp(this, DEFAULT_SLERP_ADJUSTMENT_SPEED, maxInstantAngle = 5F)
		lookController = slerpLookController
		
		health = maxHealth * 0.5F
		bossInfo.percent = 0.5F
		
		experienceValue = 50
	}
	
	override fun registerData() {
		super.registerData()
		dataManager.register(DATA_SLEEPING, true)
		dataManager.register(DATA_DEMON_LEVEL, 0)
		dataManager.register(DATA_EYE_STATE, EYE_CLOSED)
		dataManager.register(DATA_ARM_POSITION, ARMS_LIMP)
		dataManager.register(DATA_ROTATE_TARGET_ID, Int.MIN_VALUE)
		dataManager.register(DATA_ROTATE_TARGET_SPEED, DEFAULT_SLERP_ADJUSTMENT_SPEED)
	}
	
	private fun updateDemonLevelAttributes() {
		getAttributeInstance(ATTACK_DAMAGE).baseValue = 4.0 * damageMultiplier
		experienceValue = (50 * experienceMultiplier).floorToInt()
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun livingTick() {
		val isSleeping = isSleeping
		
		if (world.isRemote) {
			val currentArmAngle = clientArmAngle.currentValue
			val targetArmAngle = when (armPosition) {
				ARMS_ATTACK -> rotationPitch - 180F
				ARMS_HUG    -> rotationPitch - 90F
				else        -> 0F
			}
			
			if (abs(targetArmAngle - currentArmAngle) < 5F) {
				clientArmAngle.update(targetArmAngle)
			}
			else {
				clientArmAngle.update(currentArmAngle + ((targetArmAngle - currentArmAngle) * 0.6F).coerceIn(-25F, 25F))
			}
		}
		else {
			if (bossPhase === Sleeping) {
				health = min(health + 0.2F, realMaxHealth)
				bossInfo.isVisible = health < realMaxHealth
			}
			else {
				bossInfo.isVisible = bossPhase !== Hibernated
			}
			
			bossInfo.percent = health / maxHealth
			
			if (ticksExisted == 1) {
				updateDemonLevelAttributes()
			}
			
			val currentTarget = attackTarget
			
			if (currentTarget == null) {
				navigator.clearPath()
				motion = motion.scale(0.9)
				
				if (!isSleeping && (bossPhase is Attacking && ++fallAsleepTimer > rand.nextInt(35, 75))) {
					fallAsleep()
				}
			}
			else if (!currentTarget.isAlive || (currentTarget is PlayerEntity && (currentTarget.isCreative || currentTarget.isSpectator))) {
				attackTarget = null
				setRotateTarget(null)
			}
			else {
				val distanceFromTargetSq = getDistanceSq(currentTarget)
				
				if (distanceFromTargetSq > square(getAttributeValue(FOLLOW_RANGE) * 0.75)) {
					val closerRange = getAttributeValue(FOLLOW_RANGE) * 0.375
					val closerCandidate = rand.nextItemOrNull(world.selectVulnerableEntities.inRange<PlayerEntity>(posVec, closerRange).filter(::canEntityBeSeen))
					
					if (closerCandidate != null) {
						attackTarget = closerCandidate
					}
				}
			}
			
			bossPhase = bossPhase.tick(this)
			spawnerParticles.tick()
			
			if (isSleepingClientProp != isSleeping) {
				isSleepingClientProp = isSleeping
			}
		}
		
		if (!isSleeping) {
			slerpLookController.setRotationParams(rotateTargetSpeed, if (eyeState == EYE_LASER) 1F else 5F)
			
			rotateTargetId.takeIf { it != Int.MIN_VALUE }?.let(world::getEntityByID)?.let {
				lookController.setLookPositionWithEntity(it, 0F, 0F)
				lookController.tick() // reduces rotation latency
			}
		}
		
		super.livingTick()
	}
	
	// Spawning
	
	override fun onAddedToWorld() {
		super.onAddedToWorld()
		rotationYawHead = rotationYaw
	}
	
	override fun onInitialSpawn(world: IServerWorld, difficulty: DifficultyInstance, reason: SpawnReason, data: ILivingEntityData?, nbt: CompoundNBT?): ILivingEntityData? {
		val yaw = ((rotationYaw + 45F).toInt() / 90) * 90F
		setPositionAndRotation(posX, posY, posZ, yaw, 0F)
		
		return super.onInitialSpawn(world, difficulty, reason, data, nbt)
	}
	
	private fun wakeUp(source: DamageSource) {
		val phase = bossPhase as? SleepingPhase ?: return
		
		if (towerCenterPos == null) {
			towerCenterPos = Pos(this).down(2).offset(horizontalFacing, 3)
		}
		
		bossPhase = phase.wakeUp()
		eyeState = EYE_OPEN
		fallAsleepTimer = 0
		lastKnockbackDashTime = world.gameTime
		attackTarget = source.trueSource as? PlayerEntity
		
		playHurtSound(source)
		return
	}
	
	private fun fallAsleep() {
		eyeState = EYE_CLOSED
		armPosition = ARMS_LIMP
		attackTarget = null
		bossPhase = Sleeping
		setRotateTarget(null)
	}
	
	fun updateDemonLevel(newLevel: Byte) {
		demonLevel = newLevel
		updateDemonLevelAttributes()
	}
	
	fun resetToSpawnAfterTerritoryReloads(spawnPoint: BlockPos) {
		if (bossPhase is Attacking || bossPhase === Sleeping) {
			fallAsleep()
			health = realMaxHealth
			
			val pos = towerCenterPos?.takeIf { world.checkNoEntityCollision(this) && world.hasNoCollisions(this) } ?: return
			val yaw = pos.center.directionTowards(spawnPoint.center).toYaw()
			
			setLocationAndAngles(pos.x + 0.5, pos.y.toDouble() + 2.5, pos.z + 0.5, yaw, 0F)
			rotationYawHead = yaw
			motion = Vec3.ZERO
		}
	}
	
	override fun canDespawn(distanceToClosestPlayerSq: Double): Boolean {
		return false
	}
	
	override fun preventDespawn(): Boolean {
		return true
	}
	
	// Battle
	
	fun forceFindNewTarget(): LivingEntity? {
		val attacker = revengeTarget as? PlayerEntity
		
		if (attacker != null) {
			revengeTarget = null
			return attacker
		}
		
		val range = getAttributeValue(FOLLOW_RANGE)
		val targets = world.selectVulnerableEntities.inRange<PlayerEntity>(posVec, range).filter(::canEntityBeSeen)
		
		return rng.nextItemOrNull(targets).also { attackTarget = it }
	}
	
	override fun setAttackTarget(newTarget: LivingEntity?) {
		super.setAttackTarget(newTarget)
		
		if (attackTarget != null) {
			fallAsleepTimer = 0
		}
	}
	
	fun setRotateTarget(target: LivingEntity?, speed: Float = DEFAULT_SLERP_ADJUSTMENT_SPEED) {
		rotateTargetId = target?.entityId ?: Int.MIN_VALUE
		rotateTargetSpeed = speed
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean {
		val attack = (bossPhase as? Attacking)?.currentAttack ?: return false
		
		lastHitKnockbackMultiplier = attack.dealtKnockbackMultiplier
		val type = attack.dealtDamageType
		val amount = getAttributeValue(ATTACK_DAMAGE).toFloat() * attack.dealtDamageMultiplier
		
		return type.dealToFrom(amount, entity, this)
	}
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean {
		if (isInvulnerableTo(source) || amount < 6F) {
			if (source.immediateSource is PlayerEntity || source.isProjectile) {
				playSound(ModSounds.MOB_ENDER_EYE_HIT_FAIL, 0.8F, rand.nextFloat(0.6F, 0.8F))
			}
			
			return false
		}
		
		wakeUp(source)
		
		if ((isDemonEye || eyeState == EYE_LASER) && (amount < 8.5F || !BanishmentEffect.canBanish(this, source))) {
			playSound(ModSounds.MOB_ENDER_EYE_HIT_FAIL, 1F, rand.nextFloat(1F, 1.7F))
			return false
		}
		
		if (bossPhase is Attacking && super.attackEntityFrom(source, amount - 2F)) {
			if (knockbackDashChance > 2 && world.gameTime - lastKnockbackDashTime >= 50L && rand.nextInt(3) != 0) {
				knockbackDashChance--
			}
			
			return true
		}
		
		return false
	}
	
	override fun isInvulnerableTo(source: DamageSource): Boolean {
		return super.isInvulnerableTo(source) || source.isProjectile || source.immediateSource !is PlayerEntity
	}
	
	fun performBlastKnockback(target: Entity, strength: Float) {
		val ratio = Vec3.xz(target.posX, target.posZ).directionTowards(Vec3.xz(posX, posZ)).scale(strength)
		
		if (target is LivingEntity) {
			target.applyKnockback(strength, ratio.x, ratio.z)
			
			if (target is PlayerEntity) {
				PacketClientLaunchInstantly(target, target.motion).sendToPlayer(target)
			}
		}
		else {
			target.addVelocity(ratio.x, strength.toDouble(), ratio.z)
		}
	}
	
	fun getLaserHit(partialTicks: Float): Vector3d {
		return entity.pick(LASER_DISTANCE, partialTicks, false).hitVec
	}
	
	fun getLaserLength(partialTicks: Float): Float {
		return getLaserHit(partialTicks).distanceTo(getEyePosition(partialTicks)).toFloat()
	}
	
	override fun onDeath(cause: DamageSource) {
		val wasDead = dead
		super.onDeath(cause)
		
		if (!wasDead && dead && !world.isRemote) {
			val centerPos = towerCenterPos!!
			
			// TODO screech
			
			EntityTechnicalTrigger(world, OBSIDIAN_TOWER_DEATH_ANIMATION).apply {
				setLocationAndAngles(centerPos.x + 0.5, centerPos.y + 0.5, centerPos.z + 0.5, 0F, 0F)
				world.addEntity(this)
			}
		}
	}
	
	// Movement
	
	override fun createNavigator(world: World): PathNavigator {
		return PathNavigateFlyingPreferBeeLineOrStrafe(this, world)
	}
	
	override fun createBodyController(): BodyController {
		return EntityBodyHeadOnly(this)
	}
	
	override fun moveRelative(friction: Float, dir: Vector3d) {
		super.moveRelative(EntityMoveFlyingForward.AIR_FRICTION, dir)
	}
	
	override fun canBePushed(): Boolean {
		return false
	}
	
	override fun getPushReaction(): PushReaction {
		return PushReaction.BLOCK
	}
	
	override fun func_241845_aY(): Boolean { // RENAME canBeCollidedWith
		return isAlive && isSleeping
	}
	
	override fun collideWithEntity(entity: Entity) {}
	override fun applyEntityCollision(entity: Entity) {}
	
	override fun addVelocity(x: Double, y: Double, z: Double) {
		super.addVelocity(x * KNOCKBACK_MP, y * KNOCKBACK_MP, z * KNOCKBACK_MP)
	}
	
	override fun applyKnockback(strength: Float, ratioX: Double, ratioZ: Double) {
		val bossPhase = bossPhase
		
		if (isSleeping || bossPhase !is Attacking || !bossPhase.currentAttack.canTakeKnockback) {
			return
		}
		
		if (bossPhase.currentAttack is Melee && rand.nextInt(knockbackDashChance) == 0 && lastKnockbackDashTime != world.gameTime) {
			super.applyKnockback(strength * 1.4F, ratioX, ratioZ)
			bossPhase.currentAttack = KnockbackDash()
			knockbackDashChance = 7
			lastKnockbackDashTime = world.gameTime
		}
		else if (!ForgeHooks.onLivingKnockBack(this, strength, ratioX, ratioZ).isCanceled) {
			motion = motion.add(Vec3.xz(-ratioX, -ratioZ).normalize().scale(KNOCKBACK_MP).withY(0.005))
			
			if (motionY > 0.05) {
				motionY = 0.05
			}
		}
	}
	
	// Boss info
	
	override fun addTrackingPlayer(player: ServerPlayerEntity) {
		super.addTrackingPlayer(player)
		bossInfo.addPlayer(player)
	}
	
	override fun removeTrackingPlayer(player: ServerPlayerEntity) {
		super.removeTrackingPlayer(player)
		bossInfo.removePlayer(player)
	}
	
	override fun setCustomName(name: ITextComponent?) {
		super.setCustomName(name)
		bossInfo.name = displayName
	}
	
	// Client (disable server-side rotation and tweak render distance)
	
	private fun isRotationDelegatedToServer(): Boolean {
		return rotateTargetId == Int.MIN_VALUE
	}
	
	@Sided(Side.CLIENT)
	override fun setPositionAndRotationDirect(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, posRotationIncrements: Int, teleport: Boolean) {
		if (isRotationDelegatedToServer()) {
			super.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport)
		}
		else {
			setPosition(x, y, z)
		}
	}
	
	@Sided(Side.CLIENT)
	override fun setHeadRotation(yaw: Float, pitch: Int) {
		if (isRotationDelegatedToServer()) {
			super.setHeadRotation(yaw, pitch)
		}
	}
	
	@Sided(Side.CLIENT)
	override fun isInRangeToRenderDist(distance: Double): Boolean {
		return eyeState == EYE_LASER || super.isInRangeToRenderDist(distance)
	}
	
	@Sided(Side.CLIENT)
	override fun getRenderBoundingBox(): AxisAlignedBB {
		return if (eyeState == EYE_LASER)
			super.getRenderBoundingBox().grow(LASER_DISTANCE)
		else
			super.getRenderBoundingBox()
	}
	
	// Properties
	
	override fun getLootTable(): ResourceLocation {
		return Resource.Custom("entities/ender_eye") // TODO demon eye
	}
	
	override fun getCreatureAttribute(): CreatureAttribute {
		return if (isDemonEye)
			CustomCreatureType.DEMON
		else
			CustomCreatureType.ENDER
	}
	
	override fun getStandingEyeHeight(pose: Pose, size: net.minecraft.entity.EntitySize): Float {
		return size.height * 0.5F
	}
	
	override fun isSleeping(): Boolean {
		return if (world.isRemote) isSleepingClientProp else bossPhase is SleepingPhase
	}
	
	override fun canChangeDimension(): Boolean {
		return false
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		towerCenterPos?.let {
			putPos(TOWER_CENTER_POS_TAG, it)
		}
		
		putShort(TOTAL_SPAWNERS_TAG, totalSpawners)
		put(SPAWNER_PARTICLES_TAG, spawnerParticles.serializeNBT())
		
		putFloat(REAL_MAX_HEALTH_TAG, realMaxHealth)
		putBoolean(SLEEPING_TAG, isSleeping)
		putByte(DEMON_LEVEL_TAG, demonLevel)
		
		putString(PHASE_TAG, when (bossPhase) {
			Hibernated   -> "Hibernated"
			is OpenEye   -> "OpenEye"
			is Spawners  -> "Spawners"
			is Floating  -> "Floating"
			is Staring   -> "Staring"
			Sleeping     -> "Sleeping"
			is Attacking -> "Attacking"
		})
		
		put(PHASE_DATA_TAG, bossPhase.serializeNBT())
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		towerCenterPos = getPosOrNull(TOWER_CENTER_POS_TAG)
		
		totalSpawners = getShort(TOTAL_SPAWNERS_TAG)
		spawnerParticles.deserializeNBT(getCompound(SPAWNER_PARTICLES_TAG))
		
		realMaxHealth = getFloat(REAL_MAX_HEALTH_TAG)
		demonLevel = getByte(DEMON_LEVEL_TAG)
		
		bossPhase = when (getString(PHASE_TAG)) {
			"Hibernated" -> Hibernated
			"OpenEye"    -> OpenEye()
			"Spawners"   -> Spawners(mutableListOf(), mutableListOf(), mutableListOf(), 0)
			"Floating"   -> Floating(0)
			"Staring"    -> Staring()
			"Sleeping"   -> Sleeping
			else         -> Attacking()
		}
		
		bossPhase.deserializeNBT(getCompound(PHASE_DATA_TAG))
		eyeState = if (isSleeping) EYE_CLOSED else EYE_OPEN
	}
}
