package chylex.hee.game.entity.living
import chylex.hee.HEE
import chylex.hee.game.entity.living.EntityMobVampireBat.BehaviorType.HOSTILE
import chylex.hee.game.entity.living.EntityMobVampireBat.BehaviorType.NEUTRAL
import chylex.hee.game.entity.living.EntityMobVampireBat.BehaviorType.PASSIVE
import chylex.hee.game.entity.living.helpers.EntityMoveFlyingBat
import chylex.hee.game.entity.util.IKnockbackMultiplier
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS_WITH_SHIELD
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.STATUS
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModEntities
import chylex.hee.system.migration.vanilla.BlockChorusPlant
import chylex.hee.system.migration.vanilla.EntityBat
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.addY
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.center
import chylex.hee.system.util.color.IRandomColor.Companion.IRandomColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.isAir
import chylex.hee.system.util.isAnyVulnerablePlayerWithinRange
import chylex.hee.system.util.isPeaceful
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.makeEffect
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItemOrNull
import chylex.hee.system.util.nextVector
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.putEnum
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.square
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.use
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.ILivingEntityData
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FLYING_SPEED
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.monster.IMob
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Difficulty.HARD
import net.minecraft.world.Difficulty.NORMAL
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.IWorld
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks
import kotlin.math.cos

class EntityMobVampireBat(type: EntityType<EntityMobVampireBat>, world: World) : EntityBat(type, world), IMob, IKnockbackMultiplier{
	@Suppress("unused")
	constructor(world: World) : this(ModEntities.VAMPIRE_BAT, world)
	
	private companion object{
		private const val MIN_ATTACK_COOLDOWN = 30
		
		private val DAMAGE_GENERAL = Damage(PEACEFUL_EXCLUSION, *ALL_PROTECTIONS_WITH_SHIELD, STATUS {
			when(it.world.difficulty){
				NORMAL -> if (it.rng.nextInt(12) == 0) Potions.POISON.makeEffect(it.rng.nextInt(40, 80))  else null
				HARD   -> if (it.rng.nextInt(7) == 0)  Potions.POISON.makeEffect(it.rng.nextInt(50, 100)) else null
				else   -> null
			}
		})
		
		private const val BEHAVIOR_TYPE_TAG = "Type"
		private const val ATTACK_COOLDOWN_TAG = "AttackCooldown"
		
		private val PARTICLE_TICK = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(color = IRandomColor { RGB(nextInt(60, 162), nextInt(7, 71), nextInt(1, 19)) }, scale = 0.475F),
			pos = InBox(0.175F)
		)
	}
	
	enum class BehaviorType{
		PASSIVE, NEUTRAL, HOSTILE
	}
	
	// Instance
	
	private var behaviorType = NEUTRAL
	private var prevTargetPos: Vec3d? = null
	
	private val nextTargetPos: Vec3d
		get(){
			val currentTarget = attackTarget
			
			if (currentTarget != null){
				return currentTarget.posVec.addY(currentTarget.height * targetYOffsetMp.toDouble()).also { prevTargetPos = null }
			}
			
			val currentSleep = nextSleepPos
			
			if (currentSleep != null){
				return Vec3d(currentSleep.x + 0.5, currentSleep.y + 0.1, currentSleep.z + 0.5).also { prevTargetPos = null }
			}
			
			val pos = prevTargetPos?.takeIf { Pos(it).isAir(world) && it.y >= 1 }
			
			if (pos == null || pos.squareDistanceTo(posVec) < square(2.0) || rand.nextInt(30) == 0){
				return Pos(this).add(
					rand.nextInt(-7, 7),
					rand.nextInt(-2, 4),
					rand.nextInt(-7, 7)
				).let {
					Vec3d(it.x + 0.5, it.y + 0.1, it.z + 0.5)
				}.also {
					prevTargetPos = it
				}
			}
			
			return pos
		}
	
	private var nextSleepPos: BlockPos? = null
	private var sleepAttemptTimer = 0
	
	private var attackCooldown = MIN_ATTACK_COOLDOWN
	private var attackTimer = 0
	private var lastAttackHit = 0L
	private var targetYOffsetMp = 0F
	
	override val lastHitKnockbackMultiplier = 0.4F
	
	init{
		moveController = EntityMoveFlyingBat(this)
	}
	
	override fun registerAttributes(){
		super.registerAttributes()
		
		attributes.registerAttribute(ATTACK_DAMAGE)
		attributes.registerAttribute(FLYING_SPEED)
		
		getAttribute(FOLLOW_RANGE).baseValue = 14.5
		getAttribute(FLYING_SPEED).baseValue = 0.1
		updateHostilityAttributes()
	}
	
	private fun updateHostilityAttributes(){
		if (behaviorType == PASSIVE){
			getAttribute(MAX_HEALTH).baseValue = 6.0
			getAttribute(ATTACK_DAMAGE).baseValue = 0.0
			experienceValue = 0
		}
		else{
			getAttribute(MAX_HEALTH).baseValue = 4.0
			getAttribute(ATTACK_DAMAGE).baseValue = 3.0
			experienceValue = 1
		}
		
		health = maxHealth
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	// Behavior
	
	override fun tick(){
		super.tick()
		
		if (isBatHanging && Pos(this).up().getBlock(world) is BlockChorusPlant){
			posY = posY.floorToInt() + 1.25 - height // TODO some variations of chorus plants are extra thicc
		}
	}
	
	override fun livingTick(){
		super.livingTick()
		
		if (world.isRemote){
			if (isBatHanging){
				PARTICLE_TICK.spawn(Point(this, heightMp = 0.5F, amount = 1), rand)
			}
			else{
				PARTICLE_TICK.spawn(Point(this, heightMp = 0.585F + (cos(ticksExisted * 0.3F) * 0.1F), amount = 2), rand)
			}
			
			return
		}
		
		val currentTarget = attackTarget
		
		if (currentTarget == null){
			if (attackCooldown > 0 && --attackCooldown == 0){
				tryPickNextTarget()
			}
		}
		else if (getDistanceSq(currentTarget) > square(getAttribute(FOLLOW_RANGE).value) || (attackTimer > 0 && --attackTimer == 0)){
			attackTarget = null
		}
	}
	
	override fun updateAITasks(){ // blocks vanilla hanging behavior
		if (isBatHanging){
			if (!canHangUnderCurrentBlock() || isHangingDisturbed()){
				isBatHanging = false
			}
			else if (behaviorType == NEUTRAL && rand.nextInt(250) == 0){
				rotationYawHead = rand.nextFloat(0F, 360F)
			}
		}
		else{
			nextTargetPos.let { moveHelper.setMoveTo(it.x, it.y, it.z, 1.0) }
			
			val currentTarget = attackTarget
			
			if (currentTarget != null && world.totalTime - lastAttackHit >= 20L){
				val centerY = posY + height * 0.5
				val maxSqDistXZ = square(width * 2F) + currentTarget.width
				
				if (centerY >= currentTarget.posY - 0.2 &&
					centerY <= currentTarget.posY + 0.2 + currentTarget.height &&
					square(posX - currentTarget.posX) + square(posZ - currentTarget.posZ) < maxSqDistXZ &&
					attackEntityAsMob(currentTarget)
				){
					lastAttackHit = world.totalTime
				}
			}
			
			val sleepPos = nextSleepPos
			
			if (sleepPos != null){
				if (!canSleepAt(sleepPos) || --sleepAttemptTimer == 0){
					nextSleepPos = null
				}
				else if (lookPosVec.squareDistanceTo(sleepPos.center) < square(0.2) && canHangUnderCurrentBlock()){
					isBatHanging = true // TODO figure out how to fix a fucking freeze frame where the bat is a block lower than it should
					nextSleepPos = null
					motion = Vec3d.ZERO
					
					moveHelper.tick()
					moveForward = 0F
				}
			}
			else if (rand.nextInt(175) == 0){
				tryPickNextSleepPos()
			}
		}
	}
	
	// Sleep
	
	private fun tryPickNextSleepPos(){
		if (behaviorType == HOSTILE || (behaviorType == NEUTRAL && world.selectVulnerableEntities.inRange<EntityPlayer>(posVec, 32.0).any())){
			return
		}
		
		for(attempt in 1..10){
			val pos = Pos(lookPosVec.add(rand.nextVector(rand.nextFloat(0.5, 3.0))))
			
			if (canSleepAt(pos) && world.getLightFor(BLOCK, pos) <= 2){
				nextSleepPos = pos
				sleepAttemptTimer = 5 + (lookPosVec.distanceTo(pos.center) / 0.1).ceilToInt()
				return
			}
		}
	}
	
	private fun canSleepAt(pos: BlockPos): Boolean{
		return pos.isAir(world) && canHangUnderBlock(pos.up())
	}
	
	private fun canHangUnderCurrentBlock(): Boolean{
		return canHangUnderBlock(Pos(this).up())
	}
	
	private fun canHangUnderBlock(pos: BlockPos): Boolean{
		val state = pos.getState(world)
		return state.isNormalCube(world, pos) || state.block is BlockChorusPlant
	}
	
	private fun isHangingDisturbed(): Boolean{
		return isAnyVulnerablePlayerWithinRange(4.0)
	}
	
	override fun setIsBatHanging(isHanging: Boolean){
		if (world.isRemote && isBatHanging && !isHanging){
			Sounds.ENTITY_BAT_TAKEOFF.playClient(posVec, SoundCategory.NEUTRAL, volume = 0.05F, pitch = rand.nextFloat(0.8F, 1.2F))
		}
		
		super.setIsBatHanging(isHanging)
	}
	
	// Battle
	
	private fun tryPickNextTarget(){
		if (behaviorType == HOSTILE){
			attackTarget = rand.nextItemOrNull(world.selectVulnerableEntities.inRange<EntityPlayer>(posVec, 12.0).filter(::canEntityBeSeen))
		}
		else if (behaviorType == NEUTRAL && rand.nextInt(210) == 0){
			attackTarget = rand.nextItemOrNull(world.selectVulnerableEntities.inRange<EntityPlayer>(posVec, 7.0).filter(::canEntityBeSeen))
		}
		else{
			attackCooldown = 1
		}
	}
	
	override fun setAttackTarget(newTarget: EntityLivingBase?){
		if (attackTarget === newTarget){
			return
		}
		
		if (newTarget != null && (behaviorType == PASSIVE || world.isPeaceful)){
			attackCooldown = MIN_ATTACK_COOLDOWN
			return
		}
		
		super.setAttackTarget(newTarget)
		
		if (attackTarget == null){
			beginAttackCooldown()
		}
		else{
			attackTimer = when(behaviorType){
				NEUTRAL -> rand.nextInt(40, rand.nextInt(80, 120))
				HOSTILE -> rand.nextInt(80, 160)
				else -> 0
			}
			
			targetYOffsetMp = rand.nextFloat()
			nextSleepPos = null
		}
	}
	
	private fun beginAttackCooldown(){
		attackCooldown = when(behaviorType){
			NEUTRAL -> rand.nextInt(500, 1800)
			HOSTILE -> rand.nextInt(90, 150)
			else -> MIN_ATTACK_COOLDOWN
		}
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean{
		return DAMAGE_GENERAL.dealToFrom(entity, this)
	}
	
	// Spawning
	
	override fun onInitialSpawn(world: IWorld, difficulty: DifficultyInstance, reason: SpawnReason, data: ILivingEntityData?, nbt: CompoundNBT?): ILivingEntityData?{
		// TODO use onInitialSpawn for territory generation, call enablePersistence to stop despawning
		
		if (world.dimension.type === HEE.dim){
			when(TerritoryType.fromX(posX.floorToInt())){
				else -> {}
			}
		}
		
		return super.onInitialSpawn(world, difficulty, reason, data, nbt)
	}
	
	// Properties
	
	override fun getSoundCategory(): SoundCategory{
		return if (behaviorType == PASSIVE)
			SoundCategory.NEUTRAL
		else
			SoundCategory.HOSTILE
	}
	
	override fun getSoundPitch(): Float{
		return super.getSoundPitch() * 0.85F
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		putEnum(BEHAVIOR_TYPE_TAG, behaviorType)
		putInt(ATTACK_COOLDOWN_TAG, attackCooldown)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		behaviorType = getEnum<BehaviorType>(BEHAVIOR_TYPE_TAG) ?: behaviorType
		updateHostilityAttributes()
		
		attackCooldown = getInt(ATTACK_COOLDOWN_TAG).coerceAtLeast(MIN_ATTACK_COOLDOWN)
	}
}
