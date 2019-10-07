package chylex.hee.game.entity.living
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.living.behavior.EnderEyePhase
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Floating
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Hibernated
import chylex.hee.game.entity.living.behavior.EnderEyePhase.OpenEye
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Ready
import chylex.hee.game.entity.living.behavior.EnderEyePhase.Staring
import chylex.hee.game.entity.living.helpers.EntityBodyHeadless
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
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.RAPID_DAMAGE
import chylex.hee.init.ModLoot
import chylex.hee.network.client.PacketClientLaunchInstantly
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.Vec3
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getAttribute
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItemOrNull
import chylex.hee.system.util.posVec
import chylex.hee.system.util.scale
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.value
import chylex.hee.system.util.withY
import net.minecraft.block.material.EnumPushReaction
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityBodyHelper
import net.minecraft.entity.EntityFlying
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EnumCreatureAttribute
import net.minecraft.entity.IEntityLivingData
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FLYING_SPEED
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.util.DamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.BossInfo
import net.minecraft.world.BossInfoServer
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import kotlin.math.abs

class EntityBossEnderEye(world: World) : EntityFlying(world), IMob{
	companion object{
		private val DAMAGE_MELEE = Damage(DIFFICULTY_SCALING, ARMOR_PROTECTION(false), ENCHANTMENT_PROTECTION)
		private val DAMAGE_LASER = Damage(FIRE_TYPE(5), DIFFICULTY_SCALING, *ALL_PROTECTIONS, NUDITY_DANGER, RAPID_DAMAGE(2))
		private val DAMAGE_DASH = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION)
		
		private val DATA_SLEEPING = EntityData.register<EntityBossEnderEye, Boolean>(DataSerializers.BOOLEAN)
		private val DATA_DEMON_LEVEL = EntityData.register<EntityBossEnderEye, Byte>(DataSerializers.BYTE)
		private val DATA_ARM_POSITION = EntityData.register<EntityBossEnderEye, Byte>(DataSerializers.BYTE)
		private val DATA_ROTATE_TARGET_ID = EntityData.register<EntityBossEnderEye, Int>(DataSerializers.VARINT)
		
		private val DEMON_LEVEL_DMG = arrayOf(1.0F, 1.15F, 1.3F, 1.5F, 1.75F, 2.0F)
		private val DEMON_LEVEL_XP  = arrayOf(1.0F, 1.2F,  1.5F, 1.8F, 2.0F,  2.2F)
		
		private const val KNOCKBACK_MP = 0.15
		
		const val ARMS_LIMP: Byte = 0
		const val ARMS_HUG: Byte = 1
		const val ARMS_ATTACK: Byte = 2
	}
	
	// Instance
	
	private val bossInfo = BossInfoServer(displayName, BossInfo.Color.PINK, BossInfo.Overlay.PROGRESS).apply { setVisible(false) }
	
	var isSleeping by EntityData(DATA_SLEEPING)
		private set
	
	var demonLevel by EntityData(DATA_DEMON_LEVEL)
		private set
	
	var armPosition by EntityData(DATA_ARM_POSITION)
	val clientArmAngle = LerpedFloat(0F)
	
	private var rotateTargetId by EntityData(DATA_ROTATE_TARGET_ID)
	
	private val damageMultiplier
		get() = DEMON_LEVEL_DMG.getOrElse(demonLevel.toInt()){ 1F }
	
	private val experienceMultiplier
		get() = DEMON_LEVEL_XP.getOrElse(demonLevel.toInt()){ 1F }
	
	private var bossPhase: EnderEyePhase = Hibernated
	private var fallAsleepTimer = 0
	
	init{
		setSize(1.1F, 1F)
		isImmuneToFire = true
		
		moveHelper = EntityMoveFlyingForward(this)
		lookHelper = EntityLookSlerp(this, adjustmentSpeed = 0.5F, maxInstantAngle = 5F)
		
		health = maxHealth * 0.5F
		bossInfo.percent = 0.5F
	}
	
	override fun entityInit(){
		super.entityInit()
		dataManager.register(DATA_SLEEPING, true)
		dataManager.register(DATA_DEMON_LEVEL, 0)
		dataManager.register(DATA_ARM_POSITION, ARMS_LIMP)
		dataManager.register(DATA_ROTATE_TARGET_ID, Int.MIN_VALUE)
	}
	
	override fun applyEntityAttributes(){
		super.applyEntityAttributes()
		
		attributeMap.registerAttribute(ATTACK_DAMAGE)
		attributeMap.registerAttribute(FLYING_SPEED)
		
		getAttribute(MAX_HEALTH).baseValue = 300.0
		getAttribute(ATTACK_DAMAGE).baseValue = 4.0
		getAttribute(FLYING_SPEED).baseValue = 0.0925
		getAttribute(FOLLOW_RANGE).baseValue = 16.0
		
		experienceValue = 50
	}
	
	private fun updateDemonLevelAttributes(){
		getAttribute(ATTACK_DAMAGE).baseValue = 4.0 * damageMultiplier
		experienceValue = (50 * experienceMultiplier).floorToInt()
	}
	
	override fun onLivingUpdate(){
		val isSleeping = isSleeping
		
		if (isSleeping){
			bossInfo.setVisible(false)
		}
		else{
			bossInfo.setVisible(true)
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
			
			if (!isSleeping){
				rotateTargetId.takeIf { it != Int.MIN_VALUE }?.let(world::getEntityByID)?.let {
					lookHelper.setLookPositionWithEntity(it, 0F, 0F)
					lookHelper.onUpdateLook() // reduces rotation latency
				}
			}
		}
		else{
			if (ticksExisted == 1){
				updateDemonLevelAttributes()
			}
			
			val currentTarget = attackTarget
			
			if (currentTarget == null){
				if (!isSleeping && (bossPhase is Ready && ++fallAsleepTimer > rand.nextInt(35, 75))){
					this.isSleeping = true
				}
			}
			else if (!currentTarget.isEntityAlive || (currentTarget is EntityPlayer && (currentTarget.isCreative || currentTarget.isSpectator))){
				attackTarget = null
				setRotateTarget(null)
			}
			
			bossPhase = bossPhase.tick(this)
		}
		
		super.onLivingUpdate()
	}
	
	// Spawning
	
	override fun onInitialSpawn(difficulty: DifficultyInstance, data: IEntityLivingData?): IEntityLivingData?{
		val yaw = ((rotationYaw + 45F).toInt() / 90) * 90F
		setPositionAndRotation(posX, posY, posZ, yaw, 0F)
		
		return super.onInitialSpawn(difficulty, data)
	}
	
	private fun wakeUp(source: DamageSource){
		if (!(isSleeping || bossPhase == Hibernated)){
			return
		}
		
		isSleeping = false
		fallAsleepTimer = 0
		
		if (bossPhase !is Ready){
			bossPhase = OpenEye()
			attackTarget = source.trueSource as? EntityPlayer
		}
	}
	
	fun updateDemonLevel(newLevel: Byte){
		demonLevel = newLevel
		updateDemonLevelAttributes()
	}
	
	override fun canDespawn(): Boolean{
		return false
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
		return DAMAGE_MELEE.dealToFrom(entity, this)
	}
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean{
		if (isEntityInvulnerable(source) || amount < 6F){
			return false
		}
		
		wakeUp(source)
		return bossPhase is Ready && super.attackEntityFrom(source, amount)
	}
	
	override fun isEntityInvulnerable(source: DamageSource): Boolean{
		return super.isEntityInvulnerable(source) || source.isProjectile || source.immediateSource !is EntityPlayer
	}
	
	fun performBlastKnockback(target: Entity, strength: Float){
		val ratio = Vec3.fromXZ(target.posX, target.posZ).directionTowards(Vec3.fromXZ(posX, posZ)).scale(strength)
		
		if (target is EntityLivingBase){
			target.knockBack(this, strength, ratio.x, ratio.z)
			
			if (target is EntityPlayer){
				PacketClientLaunchInstantly(target, target.motionVec).sendToPlayer(target)
			}
		}
		else{
			target.addVelocity(ratio.x, strength.toDouble(), ratio.z)
		}
	}
	
	// Movement
	
	override fun createBodyHelper(): EntityBodyHelper{
		return EntityBodyHeadless(this)
	}
	
	override fun moveRelative(strafe: Float, up: Float, forward: Float, friction: Float){
		super.moveRelative(strafe, up, forward, EntityMoveFlyingForward.AIR_FRICTION)
	}
	
	override fun canBePushed(): Boolean{
		return false
	}
	
	override fun getPushReaction(): EnumPushReaction{
		return EnumPushReaction.BLOCK
	}
	
	override fun getCollisionBoundingBox(): AxisAlignedBB?{
		return entityBoundingBox.takeIf { isEntityAlive && isSleeping }
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
		
		if (!ForgeHooks.onLivingKnockBack(this, entity, strength, xRatio, zRatio).isCanceled){
			motionVec = motionVec.add(Vec3.fromXZ(-xRatio, -zRatio).normalize().scale(KNOCKBACK_MP).withY(0.005))
			
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
	
	override fun setCustomNameTag(name: String){
		super.setCustomNameTag(name)
		bossInfo.name = displayName
	}
	
	// Properties
	
	override fun getLootTable(): ResourceLocation{
		return ModLoot.ENDER_EYE
	}
	
	override fun getCreatureAttribute(): EnumCreatureAttribute{
		return CustomCreatureType.ENDER
	}
	
	override fun getEyeHeight(): Float{
		return height * 0.5F
	}
	
	// Serialization
	
	override fun writeEntityToNBT(nbt: TagCompound) = with(nbt.heeTag){
		super.writeEntityToNBT(nbt)
		
		setBoolean("Sleeping", isSleeping)
		setByte("DemonLevel", demonLevel)
		
		setString("Phase", when(bossPhase){
			Hibernated -> "Hibernated"
			is OpenEye -> "OpenEye"
			is Floating -> "Floating"
			is Staring -> "Staring"
			is Ready -> "Ready"
		})
		
		setTag("PhaseData", bossPhase.serializeNBT())
	}
	
	override fun readEntityFromNBT(nbt: TagCompound) = with(nbt.heeTag){
		super.readEntityFromNBT(nbt)
		
		isSleeping = getBoolean("Sleeping")
		demonLevel = getByte("DemonLevel")
		
		bossPhase = when(getString("Phase")){
			"Hibernated" -> Hibernated
			"OpenEye" -> OpenEye()
			"Floating" -> Floating(0)
			"Staring" -> Staring()
			else -> Ready()
		}
		
		bossPhase.deserializeNBT(getCompoundTag("PhaseData"))
	}
}
