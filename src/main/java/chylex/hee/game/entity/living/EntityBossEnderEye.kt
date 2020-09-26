package chylex.hee.game.entity.living
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.living.behavior.EnderEyeAttack.KnockbackDash
import chylex.hee.game.entity.living.behavior.EnderEyeAttack.Melee
import chylex.hee.game.entity.living.behavior.EnderEyePhase
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Floating
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Hibernated
import chylex.hee.game.entity.living.behavior.EnderEyePhase.OpenEye
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Ready
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Spawners
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Staring
import chylex.hee.game.entity.living.behavior.EnderEyeSpawnerParticles
import chylex.hee.game.entity.living.helpers.EntityBodyHeadOnly
import chylex.hee.game.entity.living.helpers.EntityLookSlerp
import chylex.hee.game.entity.living.helpers.EntityMoveFlyingForward
import chylex.hee.game.entity.util.EntityData
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ARMOR_PROTECTION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ENCHANTMENT_PROTECTION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.FIRE_TYPE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.NUDITY_DANGER
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.RAPID_DAMAGE
import chylex.hee.game.mechanics.potion.PotionBanishment
import chylex.hee.init.ModEntities
import chylex.hee.network.client.PacketClientLaunchInstantly
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.EntityFlying
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.EntityPlayerMP
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.Vec3
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.motionY
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItemOrNull
import chylex.hee.system.util.posVec
import chylex.hee.system.util.scale
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.use
import chylex.hee.system.util.withY
import net.minecraft.block.material.PushReaction
import net.minecraft.entity.CreatureAttribute
import net.minecraft.entity.Entity
import net.minecraft.entity.EntitySize
import net.minecraft.entity.EntityType
import net.minecraft.entity.ILivingEntityData
import net.minecraft.entity.Pose
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FLYING_SPEED
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.ai.controller.BodyController
import net.minecraft.entity.monster.IMob
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.util.DamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.BossInfo
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraft.world.server.ServerBossInfo
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.fml.network.NetworkHooks
import kotlin.math.abs

class EntityBossEnderEye(type: EntityType<EntityBossEnderEye>, world: World) : EntityFlying(type, world), IMob{
	constructor(world: World) : this(ModEntities.ENDER_EYE, world)
	
	constructor(world: World, totalSpawners: Int) : this(world){
		this.totalSpawners = totalSpawners.toShort()
	}
	
	companion object{
		val DAMAGE_MELEE = Damage(DIFFICULTY_SCALING, ARMOR_PROTECTION(false), ENCHANTMENT_PROTECTION)
		val DAMAGE_LASER = Damage(FIRE_TYPE(5), DIFFICULTY_SCALING, *ALL_PROTECTIONS, NUDITY_DANGER, RAPID_DAMAGE(2))
		val DAMAGE_DASH = Damage(DIFFICULTY_SCALING, ARMOR_PROTECTION(false))
		
		private val DATA_SLEEPING = EntityData.register<EntityBossEnderEye, Boolean>(DataSerializers.BOOLEAN)
		private val DATA_DEMON_LEVEL = EntityData.register<EntityBossEnderEye, Byte>(DataSerializers.BYTE)
		private val DATA_ARM_POSITION = EntityData.register<EntityBossEnderEye, Byte>(DataSerializers.BYTE)
		private val DATA_ROTATE_TARGET_ID = EntityData.register<EntityBossEnderEye, Int>(DataSerializers.VARINT)
		
		private val DEMON_LEVEL_DMG = arrayOf(1.0F, 1.15F, 1.3F, 1.5F, 1.75F, 2.0F)
		private val DEMON_LEVEL_XP  = arrayOf(1.0F, 1.2F,  1.5F, 1.8F, 2.0F,  2.2F)
		
		private const val KNOCKBACK_MP = 0.15
		
		private const val TOTAL_SPAWNERS_TAG = "TotalSpawners"
		private const val SPAWNER_PARTICLES_TAG = "SpawnerParticles"
		private const val REAL_MAX_HEALTH_TAG = "RealMaxHealth"
		private const val SLEEPING_TAG = "Sleeping"
		private const val DEMON_LEVEL_TAG = "DemonLevel"
		private const val PHASE_TAG = "Phase"
		private const val PHASE_DATA_TAG = "PhaseData"
		
		const val DEMON_EYE_LEVEL = 99.toByte()
		
		const val ARMS_LIMP: Byte = 0
		const val ARMS_HUG: Byte = 1
		const val ARMS_ATTACK: Byte = 2
	}
	
	// Instance
	
	private val bossInfo = ServerBossInfo(displayName, BossInfo.Color.PINK, BossInfo.Overlay.PROGRESS).apply { isVisible = false }
	
	var totalSpawners: Short = 0
		private set
	
	var realMaxHealth = 0F
	
	var isSleepingProp by EntityData(DATA_SLEEPING)
		private set
	
	var demonLevel by EntityData(DATA_DEMON_LEVEL)
		private set
	
	val isDemonEye
		get() = demonLevel == DEMON_EYE_LEVEL
	
	var armPosition by EntityData(DATA_ARM_POSITION)
	val clientArmAngle = LerpedFloat(0F)
	
	private var rotateTargetId by EntityData(DATA_ROTATE_TARGET_ID)
	
	val spawnerParticles = EnderEyeSpawnerParticles(this)
	
	private val damageMultiplier
		get() = if (isDemonEye) 2.5F else DEMON_LEVEL_DMG.getOrElse(demonLevel.toInt()){ 1F }
	
	private val experienceMultiplier
		get() = if (isDemonEye) 6F else DEMON_LEVEL_XP.getOrElse(demonLevel.toInt()){ 1F }
	
	private var bossPhase: EnderEyePhase = Hibernated
	private var fallAsleepTimer = 0
	private var knockbackDashChance = 5 // slightly higher chance of knockback after fight (re)starts
	private var lastKnockbackDashTime = 0L
	
	init{
		moveController = EntityMoveFlyingForward(this)
		lookController = EntityLookSlerp(this, adjustmentSpeed = 0.5F, maxInstantAngle = 5F)
		
		health = maxHealth * 0.5F
		bossInfo.percent = 0.5F
	}
	
	override fun registerData(){
		super.registerData()
		dataManager.register(DATA_SLEEPING, true)
		dataManager.register(DATA_DEMON_LEVEL, 0)
		dataManager.register(DATA_ARM_POSITION, ARMS_LIMP)
		dataManager.register(DATA_ROTATE_TARGET_ID, Int.MIN_VALUE)
	}
	
	override fun registerAttributes(){
		super.registerAttributes()
		
		attributes.registerAttribute(ATTACK_DAMAGE)
		attributes.registerAttribute(FLYING_SPEED)
		
		getAttribute(MAX_HEALTH).baseValue = 300.0
		getAttribute(ATTACK_DAMAGE).baseValue = 4.0
		getAttribute(FLYING_SPEED).baseValue = 0.093
		getAttribute(FOLLOW_RANGE).baseValue = 16.0
		
		experienceValue = 50
	}
	
	private fun updateDemonLevelAttributes(){
		getAttribute(ATTACK_DAMAGE).baseValue = 4.0 * damageMultiplier
		experienceValue = (50 * experienceMultiplier).floorToInt()
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun livingTick(){
		val isSleeping = isSleeping
		
		if (isSleeping){
			bossInfo.isVisible = false
		}
		else{
			bossInfo.isVisible = true
			bossInfo.percent = health / maxHealth
		}
		
		if (world.isRemote){
			val currentArmAngle = clientArmAngle.currentValue
			val targetArmAngle = when(armPosition){
				ARMS_ATTACK -> rotationPitch - 180F
				ARMS_HUG -> rotationPitch - 90F
				else -> 0F
			}
			
			if (abs(targetArmAngle - currentArmAngle) < 5F){
				clientArmAngle.update(targetArmAngle)
			}
			else{
				clientArmAngle.update(currentArmAngle + ((targetArmAngle - currentArmAngle) * 0.6F).coerceIn(-25F, 25F))
			}
		}
		else{
			if (ticksExisted == 1){
				updateDemonLevelAttributes()
			}
			
			val currentTarget = attackTarget
			
			if (currentTarget == null){
				if (!isSleeping && (bossPhase is Ready && ++fallAsleepTimer > rand.nextInt(35, 75))){
					isSleepingProp = true
					attackTarget = null
				}
			}
			else if (!currentTarget.isAlive || (currentTarget is EntityPlayer && (currentTarget.isCreative || currentTarget.isSpectator))){
				attackTarget = null
				setRotateTarget(null)
			}
			
			bossPhase = bossPhase.tick(this)
			spawnerParticles.tick()
		}
		
		if (!isSleeping){
			rotateTargetId.takeIf { it != Int.MIN_VALUE }?.let(world::getEntityByID)?.let {
				lookController.setLookPositionWithEntity(it, 0F, 0F)
				lookController.tick() // reduces rotation latency
			}
		}
		
		super.livingTick()
	}
	
	// Spawning
	
	override fun onInitialSpawn(world: IWorld, difficulty: DifficultyInstance, reason: SpawnReason, data: ILivingEntityData?, nbt: CompoundNBT?): ILivingEntityData?{
		val yaw = ((rotationYaw + 45F).toInt() / 90) * 90F
		
		setPositionAndRotation(posX, posY, posZ, yaw, 0F)
		rotationYawHead = yaw
		
		return super.onInitialSpawn(world, difficulty, reason, data, nbt)
	}
	
	private fun wakeUp(source: DamageSource){
		if (!(isSleeping || bossPhase == Hibernated)){
			return
		}
		
		isSleepingProp = false
		fallAsleepTimer = 0
		
		if (bossPhase !is Ready){
			bossPhase = OpenEye()
		}
		
		attackTarget = source.trueSource as? EntityPlayer
	}
	
	fun updateDemonLevel(newLevel: Byte){
		demonLevel = newLevel
		updateDemonLevelAttributes()
	}
	
	override fun canDespawn(distanceToClosestPlayerSq: Double): Boolean{
		return false
	}
	
	override fun preventDespawn(): Boolean{
		return true
	}
	
	// Battle
	
	fun forceFindNewTarget(): EntityPlayer?{
		val attacker = revengeTarget as? EntityPlayer
		
		if (attacker != null){
			revengeTarget = null
			return attacker
		}
		
		val range = getAttribute(FOLLOW_RANGE).value
		val targets = world.selectVulnerableEntities.inRange<EntityPlayer>(posVec, range).filter(::canEntityBeSeen)
		
		return rng.nextItemOrNull(targets).also { attackTarget = it }
	}
	
	override fun setAttackTarget(newTarget: EntityLivingBase?){
		super.setAttackTarget(newTarget)
		
		if (attackTarget != null){
			fallAsleepTimer = 0
		}
	}
	
	fun setRotateTarget(target: EntityLivingBase?){
		rotateTargetId = target?.entityId ?: Int.MIN_VALUE
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean{
		val attack = (bossPhase as? Ready)?.currentAttack ?: return false
		val amount = getAttribute(ATTACK_DAMAGE).value.toFloat() * attack.damageMultiplier
		return attack.damageType.dealToFrom(amount, entity, this)
	}
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean{
		if (isInvulnerableTo(source) || amount < 6F){
			return false
		}
		
		wakeUp(source)
		
		if (isDemonEye && (amount < 8.5F || !PotionBanishment.canBanish(this, source))){
			return false
		}
		
		if (bossPhase is Ready && super.attackEntityFrom(source, amount - 2F)){
			if (knockbackDashChance > 2 && world.totalTime - lastKnockbackDashTime >= 50L && rand.nextInt(3) != 0){
				knockbackDashChance--
			}
			
			return true
		}
		
		return false
	}
	
	override fun isInvulnerableTo(source: DamageSource): Boolean{
		return super.isInvulnerableTo(source) || source.isProjectile || source.immediateSource !is EntityPlayer
	}
	
	fun performBlastKnockback(target: Entity, strength: Float){
		val ratio = Vec3.fromXZ(target.posX, target.posZ).directionTowards(Vec3.fromXZ(posX, posZ)).scale(strength)
		
		if (target is EntityLivingBase){
			target.knockBack(this, strength, ratio.x, ratio.z)
			
			if (target is EntityPlayer){
				PacketClientLaunchInstantly(target, target.motion).sendToPlayer(target)
			}
		}
		else{
			target.addVelocity(ratio.x, strength.toDouble(), ratio.z)
		}
	}
	
	// Movement
	
	override fun createBodyController(): BodyController{
		return EntityBodyHeadOnly(this)
	}
	
	override fun moveRelative(friction: Float, dir: Vec3d){
		super.moveRelative(EntityMoveFlyingForward.AIR_FRICTION, dir)
	}
	
	override fun canBePushed(): Boolean{
		return false
	}
	
	override fun getPushReaction(): PushReaction{
		return PushReaction.BLOCK
	}
	
	override fun getCollisionBoundingBox(): AxisAlignedBB?{
		return boundingBox.takeIf { isAlive && isSleeping }
	}
	
	override fun collideWithEntity(entity: Entity){}
	override fun applyEntityCollision(entity: Entity){}
	
	override fun addVelocity(x: Double, y: Double, z: Double){
		super.addVelocity(x * KNOCKBACK_MP, y * KNOCKBACK_MP, z * KNOCKBACK_MP)
	}
	
	override fun knockBack(entity: Entity, strength: Float, xRatio: Double, zRatio: Double){
		val bossPhase = bossPhase
		
		if (isSleeping || bossPhase !is Ready || !bossPhase.currentAttack.canTakeKnockback){
			return
		}
		
		if (bossPhase.currentAttack is Melee && rand.nextInt(knockbackDashChance) == 0){
			super.knockBack(entity, strength * 1.4F, xRatio, zRatio)
			bossPhase.currentAttack = KnockbackDash()
			knockbackDashChance = 7
			lastKnockbackDashTime = world.totalTime
		}
		else if (!ForgeHooks.onLivingKnockBack(this, entity, strength, xRatio, zRatio).isCanceled){
			motion = motion.add(Vec3.fromXZ(-xRatio, -zRatio).normalize().scale(KNOCKBACK_MP).withY(0.005))
			
			if (motionY > 0.05){
				motionY = 0.05
			}
		}
	}
	
	// Boss info
	
	override fun addTrackingPlayer(player: EntityPlayerMP){
		super.addTrackingPlayer(player)
		bossInfo.addPlayer(player)
	}
	
	override fun removeTrackingPlayer(player: EntityPlayerMP){
		super.removeTrackingPlayer(player)
		bossInfo.removePlayer(player)
	}
	
	override fun setCustomName(name: ITextComponent?){
		super.setCustomName(name)
		bossInfo.name = displayName
	}
	
	// Client (disable server-side rotation)
	
	@Sided(Side.CLIENT)
	override fun setPositionAndRotationDirect(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, posRotationIncrements: Int, teleport: Boolean){
		if (rotateTargetId == Int.MIN_VALUE){
			super.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport)
		}
		else{
			setPosition(x, y, z)
		}
	}
	
	@Sided(Side.CLIENT)
	override fun setHeadRotation(yaw: Float, pitch: Int){
		if (rotateTargetId == Int.MIN_VALUE){
			super.setHeadRotation(yaw, pitch)
		}
	}
	
	// Properties
	
	override fun getLootTable(): ResourceLocation{
		return Resource.Custom("entities/ender_eye") // TODO demon eye
	}
	
	override fun getCreatureAttribute(): CreatureAttribute{
		return if (isDemonEye)
			CustomCreatureType.DEMON
		else
			CustomCreatureType.ENDER
	}
	
	override fun getStandingEyeHeight(pose: Pose, size: EntitySize): Float{
		return size.height * 0.5F
	}
	
	override fun isSleeping(): Boolean{
		return isSleepingProp
	}
	
	override fun isNonBoss(): Boolean{
		return false
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		putShort(TOTAL_SPAWNERS_TAG, totalSpawners)
		put(SPAWNER_PARTICLES_TAG, spawnerParticles.serializeNBT())
		
		putFloat(REAL_MAX_HEALTH_TAG, realMaxHealth)
		putBoolean(SLEEPING_TAG, isSleeping)
		putByte(DEMON_LEVEL_TAG, demonLevel)
		
		putString(PHASE_TAG, when(bossPhase){
			Hibernated -> "Hibernated"
			is OpenEye -> "OpenEye"
			is Spawners -> "Spawners"
			is Floating -> "Floating"
			is Staring -> "Staring"
			is Ready -> "Ready"
		})
		
		put(PHASE_DATA_TAG, bossPhase.serializeNBT())
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		totalSpawners = getShort(TOTAL_SPAWNERS_TAG)
		spawnerParticles.deserializeNBT(getCompound(SPAWNER_PARTICLES_TAG))
		
		realMaxHealth = getFloat(REAL_MAX_HEALTH_TAG)
		isSleepingProp = getBoolean(SLEEPING_TAG)
		demonLevel = getByte(DEMON_LEVEL_TAG)
		
		bossPhase = when(getString(PHASE_TAG)){
			"Hibernated" -> Hibernated
			"OpenEye" -> OpenEye()
			"Spawners" -> Spawners(mutableListOf(), mutableListOf(), mutableListOf(), 0)
			"Floating" -> Floating(0)
			"Staring" -> Staring()
			else -> Ready()
		}
		
		bossPhase.deserializeNBT(getCompound(PHASE_DATA_TAG))
	}
}
