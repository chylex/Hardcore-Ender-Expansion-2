package chylex.hee.game.entity.living
import chylex.hee.HEE
import chylex.hee.game.entity.IMobBypassPeacefulDespawn
import chylex.hee.game.entity.living.ai.AIAttackLeap
import chylex.hee.game.entity.living.ai.AIWanderLightStartle
import chylex.hee.game.entity.living.ai.AIWanderLightStartle.ILightStartleHandler
import chylex.hee.game.entity.living.ai.AIWanderOnFirePanic
import chylex.hee.game.entity.living.ai.path.PathNavigateGroundUnrestricted
import chylex.hee.game.entity.living.ai.util.AIToggle
import chylex.hee.game.entity.living.ai.util.AIToggle.Companion.addGoal
import chylex.hee.game.entity.util.EntityData
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.init.ModEntities
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.forge.EventResult
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.BlockWeb
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityMob
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.ItemAxe
import chylex.hee.system.migration.vanilla.ItemSword
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.AIAttackMelee
import chylex.hee.system.util.AISwim
import chylex.hee.system.util.AITargetAttacker
import chylex.hee.system.util.AITargetNearby
import chylex.hee.system.util.AIWanderLandStopNear
import chylex.hee.system.util.AIWatchClosest
import chylex.hee.system.util.AIWatchIdle
import chylex.hee.system.util.OPERATION_MUL_INCR_INDIVIDUAL
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.addY
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.isLoaded
import chylex.hee.system.util.isPeaceful
import chylex.hee.system.util.motionY
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectEntities
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.square
import chylex.hee.system.util.toRadians
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.tryApplyModifier
import chylex.hee.system.util.tryRemoveModifier
import chylex.hee.system.util.use
import net.minecraft.block.BlockState
import net.minecraft.entity.CreatureAttribute
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.network.IPacket
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.pathfinding.PathNavigator
import net.minecraft.potion.EffectInstance
import net.minecraft.util.DamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceContext.BlockMode
import net.minecraft.util.math.RayTraceContext.FluidMode
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Difficulty.HARD
import net.minecraft.world.Difficulty.NORMAL
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.LightType.SKY
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.event.entity.player.CriticalHitEvent
import net.minecraftforge.fml.network.NetworkHooks
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sin

class EntityMobSpiderling(type: EntityType<EntityMobSpiderling>, world: World) : EntityMob(type, world), ILightStartleHandler, IMobBypassPeacefulDespawn{
	constructor(world: World) : this(ModEntities.SPIDERLING, world)
	
	@SubscribeAllEvents(modid = HEE.ID)
	companion object{
		private val DAMAGE_GENERAL = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS)
		private val FALL_CRIT_DAMAGE = AttributeModifier("Fall crit damage", 0.5, OPERATION_MUL_INCR_INDIVIDUAL)
		
		private val DATA_SLEEPING = EntityData.register<EntityMobSpiderling, Boolean>(DataSerializers.BOOLEAN)
		
		val LOOT_TABLE = Resource.Custom("entities/spiderling")
		
		private const val BASE_JUMP_MOTION = 0.38F
		
		private const val SLEEP_STATE_TAG = "SleepState"
		private const val LIGHT_STARTLE_RESET_TIME_TAG = "LightStartleResetTime"
		
		@SubscribeEvent
		fun onHit(e: CriticalHitEvent){
			if (e.target is EntityMobSpiderling){
				e.result = EventResult.ALLOW // UPDATE abusing crit to disable sweep
			}
		}
		
		private fun findTopBlockMaxY(world: World, pos: BlockPos): Double?{
			for(y in 0..3){
				val testPos = pos.up(y)
				val testBox = testPos.getState(world).getCollisionShape(world, testPos)
				
				if (testBox.isEmpty){
					return testPos.y + testBox.boundingBox.maxY // UPDATE test
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
	
	init{
		stepHeight = 0F
	}
	
	override fun registerData(){
		super.registerData()
		dataManager.register(DATA_SLEEPING, true)
	}
	
	override fun registerAttributes(){
		super.registerAttributes()
		
		getAttribute(MAX_HEALTH).baseValue = rand.nextInt(11, 13).toDouble()
		getAttribute(ATTACK_DAMAGE).baseValue = 1.5
		getAttribute(MOVEMENT_SPEED).baseValue = 0.32
		getAttribute(FOLLOW_RANGE).baseValue = 20.0
		
		experienceValue = 2
	}
	
	override fun registerGoals(){
		aiMovement = AIToggle()
		aiMovement.enabled = false
		
		goalSelector.addGoal(1, AISwim(this))
		goalSelector.addGoal(2, AIWanderOnFirePanic(this, movementSpeed = 1.2, searchWaterChance = ::panicSearchWaterChance, maxWaterDistanceXZ = 20, maxLandDistanceY = 5, searchLandChance = ::panicSearchLandChance, maxLandDistanceXZ = 5, maxWaterDistanceY = 2))
		goalSelector.addGoal(3, AIWanderLightStartle(this, movementSpeed = 1.2, minBlockLightIncrease = 3, minCombinedLightDecrease = 6, searchAttempts = 1000, maxDistanceXZ = 15, maxDistanceY = 2, handler = this))
		goalSelector.addGoal(4, AIAttackLeap(this, triggerDistance = (1.5)..(3.5), triggerChance = 0.75F, triggerCooldown = 45, leapStrengthXZ = (0.9)..(1.1), leapStrengthY = (0.4)..(0.5)), aiMovement)
		goalSelector.addGoal(5, AIAttackMelee(this, movementSpeed = 1.1, chaseAfterLosingSight = true), aiMovement)
		goalSelector.addGoal(6, AIWanderLandStopNear<EntityPlayer>(this, movementSpeed = 0.9, chancePerTick = 30, detectDistance = 7.5), aiMovement)
		goalSelector.addGoal(7, AIWatchClosest<EntityPlayer>(this, maxDistance = 3.5F), aiMovement)
		goalSelector.addGoal(8, AIWatchIdle(this), aiMovement)
		
		targetSelector.addGoal(1, AITargetAttacker(this, callReinforcements = false))
		targetSelector.addGoal(2, AITargetNearby(this, chancePerTick = 18, checkSight = true, easilyReachableOnly = false, targetPredicate = ::isPlayerNearbyForAttack), aiMovement)
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	// Behavior (General)
	
	override fun livingTick(){
		super.livingTick()
		
		if (world.isRemote){
			if (ticksExisted == 1){ // fix first tick body rotation delay
				rotationYaw = rotationYawHead
				setRenderYawOffset(rotationYawHead)
			}
			
			return
		}
		
		if (world.isPeaceful && attackTarget != null){
			super.setAttackTarget(null)
			lightStartleResetTime = 0L
		}
		
		if (wakeUpDelayAI > 0){
			if (--wakeUpDelayAI == 0){
				aiMovement.enabled = true
			}
		}
		else if (wakeUpTimer > 0){
			if (--wakeUpTimer == 0){
				wakeUp(instant = false, preventSleep = false)
			}
		}
		else if (ticksExisted % 4 == 0){
			if (isSleeping){
				if (wakeUpTimer == 0){
					val nearestPlayer = findPlayerInSight(maxDistance = 8.0, maxDiffY = 3)
					
					if (nearestPlayer != null){
						onDisturbed()
					}
				}
			}
			else if (canSleepAgain && navigator.noPath() && rand.nextInt(6) == 0){
				if (findPlayerInSight(maxDistance = 16.0, maxDiffY = 6) == null){
					isSleepingProp = true
					aiMovement.enabled = false
				}
			}
		}
		
		if (onGround){
			getAttribute(ATTACK_DAMAGE).tryRemoveModifier(FALL_CRIT_DAMAGE)
		}
		
		if (jumpCooldown > 0){
			--jumpCooldown
		}
		
		if (jumpCooldown == 0 && onGround && ticksExisted % 3 == 0){
			if (navigator.noPath()){
				jumpCooldown = 10
			}
			else{
				val start = posVec.addY(0.5)
				val target = Vec3d(moveHelper.x, start.y, moveHelper.z)
				
				val direction = start.directionTowards(target)
				val obstacle = world.rayTraceBlocks(RayTraceContext(start, start.add(direction.scale(3.0)), BlockMode.COLLIDER, FluidMode.NONE, this))
				
				if (obstacle.type == Type.BLOCK){
					val topY = findTopBlockMaxY(world, obstacle.pos)
					
					if (topY != null){
						val diffY = (topY - posY).coerceIn(0.5, 2.5)
						val jumpMotion = (0.514 * log10(diffY + 0.35)) + 0.416 // goes roughly from 0.38 for half a block, to 0.65 for two and a half blocks
						
						if (!collidedHorizontally){
							val strafeMotionMp = 0.2 + (jumpMotion - BASE_JUMP_MOTION)
							motion = motion.add(direction.x * strafeMotionMp, 0.0, direction.z * strafeMotionMp)
						}
						
						motionY = jumpMotion
						isAirBorne = true
						ForgeHooks.onLivingJump(this)
						
						jumpCooldown = rand.nextInt(18, 22)
						getAttribute(ATTACK_DAMAGE).tryApplyModifier(FALL_CRIT_DAMAGE)
					}
				}
			}
		}
	}
	
	private fun onDisturbed(){
		if (!isSleeping || wakeUpTimer != 0){
			return
		}
		
		wakeUpTimer = 5 * rand.nextInt(1, 12)
		
		for(nearbySpiderling in world.selectExistingEntities.inRange<EntityMobSpiderling>(posVec, 3.5)){
			if (nearbySpiderling !== this){
				nearbySpiderling.onDisturbed()
			}
		}
	}
	
	private fun wakeUp(instant: Boolean, preventSleep: Boolean){
		if (isSleeping){
			isSleepingProp = false
			wakeUpDelayAI = if (instant) 1 else rand.nextInt(25, 40)
			
			if (preventSleep){
				canSleepAgain = false
			}
		}
	}
	
	override fun setFire(seconds: Int){
		wakeUp(instant = true, preventSleep = true)
		super.setFire(seconds)
	}
	
	override fun isPotionApplicable(effect: EffectInstance): Boolean{
		return effect.potion != Potions.POISON && super.isPotionApplicable(effect)
	}
	
	// Behavior (Light)
	
	override fun onLightStartled(): Boolean{
		wakeUp(instant = false, preventSleep = true)
		
		if (world.totalTime < lightStartleResetTime){
			return false
		}
		
		lightStartleResetTime = 0L
		
		if (attackTarget == null){
			attackTarget = world.getClosestPlayer(this@EntityMobSpiderling, 10.0)
		}
		
		return true
	}
	
	override fun onDarknessReached(){
		val peaceChance = when(world.difficulty){
			HARD   -> 0.25F
			NORMAL -> 0.75F
			else   -> 1F
		}
		
		if (rand.nextFloat() < peaceChance){
			attackTarget = null
		}
	}
	
	// Targeting
	
	private fun isPlayerNearbyForAttack(player: EntityPlayer): Boolean{
		return getDistanceSq(player) < square(1.75)
	}
	
	private fun findPlayerInSight(maxDistance: Double, maxDiffY: Int): EntityPlayer?{
		return world.selectVulnerableEntities.inRange<EntityPlayer>(posVec, maxDistance).firstOrNull {
			abs(posY - it.posY).roundToInt() <= maxDiffY && canEntityBeSeen(it)
		}
	}
	
	override fun setAttackTarget(newTarget: EntityLivingBase?){
		if (!world.isPeaceful && newTarget !== attackTarget){
			super.setAttackTarget(newTarget)
			
			if (attackTarget == null && lightStartleResetTime == 0L){
				lightStartleResetTime = world.totalTime + (30L * 20L)
			}
		}
	}
	
	// Movement
	
	override fun createNavigator(world: World): PathNavigator{
		return PathNavigateGroundUnrestricted(this, world)
	}
	
	override fun getBlockPathWeight(pos: BlockPos): Float{
		return 25F - world.getLightFor(BLOCK, pos) - (world.getLightFor(SKY, pos) * 0.5F)
	}
	
	override fun jump(){ // TODO could improve by making the motion less smooth when starting the jump, somehow
		if (jumpCooldown == 0){
			getAttribute(ATTACK_DAMAGE).tryApplyModifier(FALL_CRIT_DAMAGE)
			super.jump()
		}
	}
	
	override fun getJumpUpwardsMotion(): Float{
		return BASE_JUMP_MOTION
	}
	
	override fun getMaxFallHeight(): Int{
		return 256
	}
	
	override fun setMotionMultiplier(state: BlockState, mp: Vec3d){
		if (state.block !is BlockWeb){
			super.setMotionMultiplier(state, mp)
		}
	}
	
	// Damage
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean{
		if (source === DamageSource.FALL){
			return false
		}
		
		wakeUp(instant = true, preventSleep = true)
		
		if (!super.attackEntityFrom(source, if (source.isFireDamage) amount * 1.25F else amount)){
			return false
		}
		
		val player = source.trueSource as? EntityPlayer
		
		if (!isBeingSwept && player != null && player.getHeldItem(MAIN_HAND).item.let { it is ItemSword || it is ItemAxe }){
			val yaw = player.rotationYaw.toDouble().toRadians()
			val xRatio = sin(yaw)
			val zRatio = -cos(yaw)
			
			for(nearby in world.selectEntities.inRange<EntityMobSpiderling>(posVec, 2.0)){
				if (nearby !== this){
					nearby.isBeingSwept = true
					nearby.knockBack(player, 0.4F, xRatio, zRatio)
					nearby.attackEntityFrom(DamageSource.causePlayerDamage(player), amount * rand.nextFloat(0.75F, 1F))
					nearby.isBeingSwept = false
				}
			}
		}
		
		return true
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean{
		if (goalSelector.runningGoals.anyMatch { it.goal is AIAttackLeap }){
			getAttribute(ATTACK_DAMAGE).tryApplyModifier(FALL_CRIT_DAMAGE)
		}
		
		if (!DAMAGE_GENERAL.dealToFrom(entity, this)){
			return false
		}
		
		canSleepAgain = false
		
		if (isBurning && world.difficulty >= NORMAL){
			entity.setFire(3)
		}
		
		return true
	}
	
	// Properties
	
	override fun getLootTable(): ResourceLocation{
		return LOOT_TABLE
	}
	
	override fun getExperiencePoints(player: EntityPlayer): Int{
		return rand.nextInt(0, experienceValue)
	}
	
	override fun getCreatureAttribute(): CreatureAttribute{
		return CreatureAttribute.ARTHROPOD
	}
	
	override fun isSleeping(): Boolean{
		return isSleepingProp
	}
	
	override fun playAmbientSound(){
		if (!isSleeping && rand.nextInt(5) <= 1){
			super.playAmbientSound()
		}
	}
	
	override fun playStepSound(pos: BlockPos, state: BlockState){
		playSound(Sounds.ENTITY_SPIDER_STEP, 0.15F, soundPitch)
	}
	
	public override fun getAmbientSound(): SoundEvent{
		return Sounds.ENTITY_SPIDER_AMBIENT
	}
	
	public override fun getHurtSound(source: DamageSource): SoundEvent{
		return Sounds.ENTITY_SPIDER_HURT
	}
	
	public override fun getDeathSound(): SoundEvent{
		return Sounds.ENTITY_SPIDER_DEATH
	}
	
	public override fun getSoundPitch(): Float{
		return rand.nextFloat(1.2F, 1.5F)
	}
	
	@Sided(Side.CLIENT)
	override fun getBrightnessForRender(): Int{
		val pos = Pos(this)
		
		if (!pos.isLoaded(world)){
			return 0
		}
		
		val sky = (world.getLightFor(SKY, pos) * 0.77).floorToInt()
		val block = (world.getLightFor(BLOCK, pos) * 0.77).floorToInt()
		
		return (sky shl 20) or (block shl 4)
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		putInt(SLEEP_STATE_TAG, when{
			isSleeping -> 2
			canSleepAgain -> 1
			else -> 0
		})
		
		putLong(LIGHT_STARTLE_RESET_TIME_TAG, lightStartleResetTime)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		val sleepState = getInt(SLEEP_STATE_TAG)
		
		if (sleepState != 2){
			wakeUp(instant = true, preventSleep = sleepState != 1)
		}
		
		lightStartleResetTime = getLong(LIGHT_STARTLE_RESET_TIME_TAG)
	}
}
