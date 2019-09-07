package chylex.hee.game.entity.living
import chylex.hee.HEE
import chylex.hee.game.entity.IMobBypassPeacefulDespawn
import chylex.hee.game.entity.living.ai.AIAttackLeap
import chylex.hee.game.entity.living.ai.AIWanderLightStartle
import chylex.hee.game.entity.living.ai.AIWanderLightStartle.ILightStartleHandler
import chylex.hee.game.entity.living.ai.AIWanderOnFirePanic
import chylex.hee.game.entity.living.ai.path.PathNavigateGroundUnrestricted
import chylex.hee.game.entity.living.ai.util.AIToggle
import chylex.hee.game.entity.living.ai.util.AIToggle.Companion.addTask
import chylex.hee.game.entity.util.EntityData
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.world.util.RayTracer
import chylex.hee.init.ModLoot
import chylex.hee.system.util.AIAttackMelee
import chylex.hee.system.util.AISwim
import chylex.hee.system.util.AITargetAttacker
import chylex.hee.system.util.AITargetNearby
import chylex.hee.system.util.AIWanderLandStopNear
import chylex.hee.system.util.AIWatchClosest
import chylex.hee.system.util.AIWatchIdle
import chylex.hee.system.util.OPERATION_MUL_INCR_INDIVIDUAL
import chylex.hee.system.util.Pos
import chylex.hee.system.util.addY
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.isLoaded
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectEntities
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.square
import chylex.hee.system.util.toRadians
import chylex.hee.system.util.tryApplyModifier
import chylex.hee.system.util.tryRemoveModifier
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EnumCreatureAttribute
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects.POISON
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.pathfinding.PathNavigate
import net.minecraft.potion.PotionEffect
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.EnumDifficulty.HARD
import net.minecraft.world.EnumDifficulty.NORMAL
import net.minecraft.world.EnumDifficulty.PEACEFUL
import net.minecraft.world.EnumSkyBlock.BLOCK
import net.minecraft.world.EnumSkyBlock.SKY
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.event.entity.player.CriticalHitEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sin

@EventBusSubscriber(modid = HEE.ID)
class EntityMobSpiderling(world: World) : EntityMob(world), ILightStartleHandler, IMobBypassPeacefulDespawn{
	companion object{
		private val DAMAGE_GENERAL = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS)
		private val FALL_CRIT_DAMAGE = AttributeModifier("Fall crit damage", 0.5, OPERATION_MUL_INCR_INDIVIDUAL)
		
		private val DATA_SLEEPING = EntityData.register<EntityMobSpiderling, Boolean>(DataSerializers.BOOLEAN)
		
		private val RAY_TRACE_OBSTACLE = RayTracer(
			canCollideCheck = { _, _, state -> state.block.canCollideCheck(state, false) }
		)
		
		private const val BASE_JUMP_MOTION = 0.38F
		
		@JvmStatic
		@SubscribeEvent
		fun onHit(e: CriticalHitEvent){
			if (e.target is EntityMobSpiderling){
				e.result = ALLOW // UPDATE abusing crit to disable sweep
			}
		}
		
		private fun findTopBlockBox(world: World, pos: BlockPos): AxisAlignedBB?{
			var topBox: AxisAlignedBB? = null
			
			for(y in 0..3){
				val testPos = pos.up(y)
				val testBox = testPos.getState(world).getCollisionBoundingBox(world, testPos)
				
				if (testBox == Block.NULL_AABB){
					return topBox?.offset(pos.up(y - 1))
				}
				else{
					topBox = testBox
				}
			}
			
			return null
		}
	}
	
	// Instance
	
	var isSleeping by EntityData(DATA_SLEEPING)
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
		setSize(0.675F, 0.45F)
		stepHeight = 0F
	}
	
	override fun entityInit(){
		super.entityInit()
		dataManager.register(DATA_SLEEPING, true)
	}
	
	override fun applyEntityAttributes(){
		super.applyEntityAttributes()
		
		getEntityAttribute(MAX_HEALTH).baseValue = rand.nextInt(11, 13).toDouble()
		getEntityAttribute(ATTACK_DAMAGE).baseValue = 1.5
		getEntityAttribute(MOVEMENT_SPEED).baseValue = 0.32
		getEntityAttribute(FOLLOW_RANGE).baseValue = 20.0
		
		experienceValue = 2
	}
	
	override fun initEntityAI(){
		aiMovement = AIToggle()
		aiMovement.enabled = false
		
		tasks.addTask(1, AISwim(this))
		tasks.addTask(2, AIWanderOnFirePanic(this, movementSpeed = 1.2, searchWaterChance = ::panicSearchWaterChance, maxWaterDistanceXZ = 20, maxLandDistanceY = 5, searchLandChance = ::panicSearchLandChance, maxLandDistanceXZ = 5, maxWaterDistanceY = 2))
		tasks.addTask(3, AIWanderLightStartle(this, movementSpeed = 1.2, minBlockLightIncrease = 3, minCombinedLightDecrease = 6, searchAttempts = 1000, maxDistanceXZ = 15, maxDistanceY = 2, handler = this))
		tasks.addTask(4, AIAttackLeap(this, triggerDistance = (1.5)..(3.5), triggerChance = 0.75F, triggerCooldown = 45, leapStrengthXZ = (0.9)..(1.1), leapStrengthY = (0.4)..(0.5)), aiMovement)
		tasks.addTask(5, AIAttackMelee(this, movementSpeed = 1.1, chaseAfterLosingSight = true), aiMovement)
		tasks.addTask(6, AIWanderLandStopNear<EntityPlayer>(this, movementSpeed = 0.9, chancePerTick = 30, detectDistance = 7.5), aiMovement)
		tasks.addTask(7, AIWatchClosest<EntityPlayer>(this, maxDistance = 3.5F), aiMovement)
		tasks.addTask(8, AIWatchIdle(this), aiMovement)
		
		targetTasks.addTask(1, AITargetAttacker(this, callReinforcements = false))
		targetTasks.addTask(2, AITargetNearby(this, chancePerTick = 18, checkSight = true, easilyReachableOnly = false, targetPredicate = ::isPlayerNearbyForAttack), aiMovement)
	}
	
	// Behavior (General)
	
	override fun onLivingUpdate(){
		super.onLivingUpdate()
		
		if (world.isRemote){
			if (ticksExisted == 1){ // fix first tick body rotation delay
				rotationYaw = rotationYawHead
				setRenderYawOffset(rotationYawHead)
			}
			
			return
		}
		
		if (world.difficulty == PEACEFUL && attackTarget != null){
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
					isSleeping = true
					aiMovement.enabled = false
				}
			}
		}
		
		if (onGround){
			getEntityAttribute(ATTACK_DAMAGE).tryRemoveModifier(FALL_CRIT_DAMAGE)
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
				val obstacle = RAY_TRACE_OBSTACLE.traceBlocksBetweenVectors(world, start, start.add(direction.scale(3.0)))
				
				if (obstacle != null){
					val topBox = findTopBlockBox(world, obstacle.blockPos)
					
					if (topBox != null){
						val diffY = (topBox.maxY - posY).coerceIn(0.5, 2.5)
						val jumpMotion = (0.514 * log10(diffY + 0.35)) + 0.416 // goes roughly from 0.38 for half a block, to 0.65 for two and a half blocks
						
						if (!collidedHorizontally){
							val strafeMotionMp = 0.2 + (jumpMotion - BASE_JUMP_MOTION)
							motionX += direction.x * strafeMotionMp
							motionZ += direction.z * strafeMotionMp
						}
						
						motionY = jumpMotion
						isAirBorne = true
						ForgeHooks.onLivingJump(this)
						
						jumpCooldown = rand.nextInt(18, 22)
						getEntityAttribute(ATTACK_DAMAGE).tryApplyModifier(FALL_CRIT_DAMAGE)
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
			isSleeping = false
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
	
	override fun isPotionApplicable(effect: PotionEffect): Boolean{
		return effect.potion != POISON && super.isPotionApplicable(effect)
	}
	
	// Behavior (Light)
	
	override fun onLightStartled(): Boolean{
		wakeUp(instant = false, preventSleep = true)
		
		if (world.totalWorldTime < lightStartleResetTime){
			return false
		}
		
		lightStartleResetTime = 0L
		
		if (attackTarget == null){
			attackTarget = world.getClosestPlayerToEntity(this@EntityMobSpiderling, 10.0)
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
		if (world.difficulty != PEACEFUL && newTarget !== attackTarget){
			super.setAttackTarget(newTarget)
			
			if (newTarget == null && lightStartleResetTime == 0L){
				lightStartleResetTime = world.totalWorldTime + (30L * 20L)
			}
		}
	}
	
	// Movement
	
	override fun createNavigator(world: World): PathNavigate{
		return PathNavigateGroundUnrestricted(this, world)
	}
	
	override fun getBlockPathWeight(pos: BlockPos): Float{
		return 25F - world.getLightFor(BLOCK, pos) - (world.getLightFor(SKY, pos) * 0.5F)
	}
	
	override fun jump(){ // TODO could improve by making the motion less smooth when starting the jump, somehow
		if (jumpCooldown == 0){
			getEntityAttribute(ATTACK_DAMAGE).tryApplyModifier(FALL_CRIT_DAMAGE)
			super.jump()
		}
	}
	
	override fun getJumpUpwardsMotion(): Float{
		return BASE_JUMP_MOTION
	}
	
	override fun getMaxFallHeight(): Int{
		return 256
	}
	
	override fun setInWeb(){}
	
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
		if (tasks.taskEntries.any { it.using && it.action is AIAttackLeap }){
			getEntityAttribute(ATTACK_DAMAGE).tryApplyModifier(FALL_CRIT_DAMAGE)
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
		return ModLoot.SPIDERLING
	}
	
	override fun getExperiencePoints(player: EntityPlayer): Int{
		return rand.nextInt(0, experienceValue)
	}
	
	override fun getCreatureAttribute(): EnumCreatureAttribute{
		return EnumCreatureAttribute.ARTHROPOD
	}
	
	override fun playLivingSound(){
		if (!isSleeping && rand.nextInt(5) <= 1){
			super.playLivingSound()
		}
	}
	
	override fun playStepSound(pos: BlockPos, block: Block){
		playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.15F, soundPitch)
	}
	
	public override fun getAmbientSound(): SoundEvent{
		return SoundEvents.ENTITY_SPIDER_AMBIENT
	}
	
	public override fun getHurtSound(source: DamageSource): SoundEvent{
		return SoundEvents.ENTITY_SPIDER_HURT
	}
	
	public override fun getDeathSound(): SoundEvent{
		return SoundEvents.ENTITY_SPIDER_DEATH
	}
	
	public override fun getSoundPitch(): Float{
		return rand.nextFloat(1.2F, 1.5F)
	}
	
	@SideOnly(Side.CLIENT)
	override fun getBrightnessForRender(): Int{
		val pos = Pos(this)
		
		if (!pos.isLoaded(world)){
			return 0
		}
		
		val sky = (world.getLightFromNeighborsFor(SKY, pos) * 0.77).floorToInt()
		val block = (world.getLightFromNeighborsFor(BLOCK, pos) * 0.77).floorToInt()
		
		return (sky shl 20) or (block shl 4)
	}
	
	// Serialization
	
	override fun writeEntityToNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		setInteger("SleepState", when{
			isSleeping -> 2
			canSleepAgain -> 1
			else -> 0
		})
		
		setLong("LightStartleResetTime", lightStartleResetTime)
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		val sleepState = getInteger("SleepState")
		
		if (sleepState != 2){
			wakeUp(instant = true, preventSleep = sleepState != 1)
		}
		
		lightStartleResetTime = getLong("LightStartleResetTime")
	}
}
