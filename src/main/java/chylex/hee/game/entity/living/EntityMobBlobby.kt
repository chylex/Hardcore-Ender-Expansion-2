package chylex.hee.game.entity.living
import chylex.hee.game.entity.living.ai.AIFollowLeaderJumping
import chylex.hee.game.entity.living.ai.AIPickUpItemDetour
import chylex.hee.game.entity.living.ai.AIWanderLand
import chylex.hee.game.entity.living.ai.AIWatchDyingLeader
import chylex.hee.game.entity.living.ai.AIWatchIdleJumping
import chylex.hee.game.entity.living.ai.path.PathNavigateGroundPreferBeeLine
import chylex.hee.game.entity.living.ai.util.AIToggle
import chylex.hee.game.entity.living.ai.util.AIToggle.Companion.addGoal
import chylex.hee.game.entity.living.behavior.BlobbyItemPickupHandler
import chylex.hee.game.entity.living.helpers.EntityBodyHeadOnly
import chylex.hee.game.entity.living.helpers.EntityLookWhileJumping
import chylex.hee.game.entity.living.helpers.EntityMoveJumping
import chylex.hee.game.entity.util.ColorDataSerializer
import chylex.hee.game.entity.util.EntityData
import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.system.migration.vanilla.EntityCreature
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.ItemSword
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.AISwim
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.Vec3
import chylex.hee.system.util.addY
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.color.IntColor.Companion.HCL
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.getFloatOrNull
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.getStack
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.nextVector2
import chylex.hee.system.util.offsetTowards
import chylex.hee.system.util.posVec
import chylex.hee.system.util.putStack
import chylex.hee.system.util.scale
import chylex.hee.system.util.selectEntities
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import chylex.hee.system.util.withY
import net.minecraft.entity.Entity
import net.minecraft.entity.EntitySize
import net.minecraft.entity.EntityType
import net.minecraft.entity.ILivingEntityData
import net.minecraft.entity.Pose
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.SpawnReason.SPAWN_EGG
import net.minecraft.entity.ai.controller.BodyController
import net.minecraft.item.ItemStack
import net.minecraft.item.UseAction.SPEAR
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.pathfinding.PathNavigator
import net.minecraft.pathfinding.PathNodeType
import net.minecraft.potion.Effects.JUMP_BOOST
import net.minecraft.util.DamageSource
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.Hand.OFF_HAND
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks
import java.util.Random
import kotlin.math.abs

class EntityMobBlobby(type: EntityType<out EntityCreature>, world: World) : EntityCreature(type, world){
	@Suppress("unused")
	constructor(world: World) : this(ModEntities.BLOBBY, world)
	
	companion object{
		private val DATA_SCALE = EntityData.register<EntityMobBlobby, Float>(DataSerializers.FLOAT)
		private val DATA_COLOR = EntityData.register<EntityMobBlobby, IntColor>(ColorDataSerializer)
		private val DATA_HELD_ITEM = EntityData.register<EntityMobBlobby, ItemStack>(DataSerializers.ITEMSTACK)
		
		val LOOT_TABLE = Resource.Custom("entities/blobby")
		
		private const val GROUP_ID_TAG = "Group"
		private const val GROUP_LEADER_TAG = "IsLeader"
		private const val MAX_HEALTH_TAG = "MaxHealth"
		private const val SCALE_TAG = "Scale"
		private const val COLOR_TAG = "Color"
		private const val JUMP_DELAY_MIN_TAG = "JumpDelayMin"
		private const val JUMP_DELAY_MAX_TAG = "JumpDelayMax"
		private const val JUMP_SPEED_TAG = "JumpSpeed"
		private const val JUMP_HEIGHT_TAG = "JumpHeight"
		private const val HELD_ITEM_TAG = "HeldItem"
		private const val ITEM_PICKUP_TAG = "ItemPickup"
		
		private val MAX_JUMP_DELAY = Int.MAX_VALUE..Int.MAX_VALUE
		
		class SpawnGroupData(genRand: Random) : ILivingEntityData{
			private val rngSeedBase = genRand.nextLong()
			private val rngSeedOffset = genRand.nextInt(100, 10000)
			
			private val groupId = genRand.nextLong()
			private val subGroups = genRand.nextInt(1, 3)
			
			private val baseHealth = genRand.nextInt(1, 6).toFloat()
			private val leaderHealth = baseHealth + genRand.nextInt(1, 2)
			
			private val scales = Array(subGroups){ genRand.nextFloat(0.55F, 1F) }.apply { sortDescending() }
			private val hues = Array(subGroups){ genRand.nextFloat(0.0, 360.0) }
			
			private val chroma = genRand.nextFloat(80F, 95F)
			private val luminance = genRand.nextFloat(51F, 59F)
			
			private val jumpDelay = genRand.nextInt(0, 5).let { (genRand.nextInt(8, 10) + it)..(genRand.nextInt(11, 14) + it) }
			private val jumpSpeedMp = 1F + (0.025 + (genRand.nextGaussian() * 0.2)).coerceIn(-0.2, 0.3).toFloat()
			private val jumpHeightMp = offsetTowards(jumpSpeedMp, 1F, 0.4F) + abs(genRand.nextGaussian() * 0.3).coerceAtMost(0.6).toFloat()
			
			fun setupLeader(blobby: EntityMobBlobby){
				blobby.groupId = groupId
				blobby.setLeaderStatusAndRefresh(true)
				
				blobby.setMaxHealth(leaderHealth, resetCurrentHealth = true)
				blobby.scale = scales[0]
				blobby.color = HCL(hues[0], chroma, luminance)
				
				blobby.jumpDelay = jumpDelay
				blobby.jumpSpeedMp = jumpSpeedMp
				blobby.jumpHeightMp = jumpHeightMp
			}
			
			fun generateLeader(world: World) = EntityMobBlobby(world).also(::setupLeader)
			
			fun generateChild(world: World, index: Int) = EntityMobBlobby(world).also {
				val rand = Random(rngSeedBase + (index * rngSeedOffset))
				val subGroup = rand.nextInt(subGroups)
				
				it.groupId = groupId
				
				it.setMaxHealth(baseHealth + rand.nextInt(0, 2), resetCurrentHealth = true)
				it.scale = scales[subGroup] * rand.nextFloat(0.91F, 0.99F)
				
				it.color = HCL(
					hues[subGroup] + rand.nextFloat(-6.0, 6.0),
					chroma * rand.nextFloat(0.94F, 1.03F),
					luminance * rand.nextFloat(0.97F, 1.018F)
				)
				
				it.jumpDelay = jumpDelay
				it.jumpSpeedMp = jumpSpeedMp * rand.nextFloat(0.93F, 1.06F)
				it.jumpHeightMp = jumpHeightMp * rand.nextFloat(0.97F, 1.03F)
			}
		}
	}
	
	// Instance
	
	private var groupId = 0L
	private var isGroupLeader = false
	
	var scale by EntityData(DATA_SCALE)
		private set
	
	var color by EntityData(DATA_COLOR)
		private set
	
	private var jumpDelay = MAX_JUMP_DELAY
	private var jumpSpeedMp = 1F
	private var jumpHeightMp = 1F
	
	var heldItem by EntityData(DATA_HELD_ITEM)
	private lateinit var itemPickupHandler: BlobbyItemPickupHandler
	
	private var isColliding = false
	private var wasOnGround = false
	
	val renderSquishClient = LerpedFloat(0F)
	private var squishTarget = 0F
	
	private lateinit var aiLeaderEnabledToggle: AIToggle
	private lateinit var aiLeaderDisabledToggle: AIToggle
	
	private val isFollowRangeAreaLoaded
		get() = world.isAreaLoaded(Pos(this), getAttribute(FOLLOW_RANGE).value.ceilToInt())
	
	// Initialization
	
	init{
		moveController = EntityMoveJumping(this, ::getJumpDelay, degreeDiffBeforeMovement = 22.5)
		lookController = EntityLookWhileJumping(this)
		
		setPathPriority(PathNodeType.WATER, 2F)
		setPathPriority(PathNodeType.WATER_BORDER, 4F)
	}
	
	override fun registerData(){
		super.registerData()
		dataManager.register(DATA_COLOR, RGB(255u))
		dataManager.register(DATA_SCALE, 1F)
		dataManager.register(DATA_HELD_ITEM, ItemStack.EMPTY)
	}
	
	override fun registerAttributes(){
		super.registerAttributes()
		
		getAttribute(MAX_HEALTH).baseValue = 8.0
		getAttribute(MOVEMENT_SPEED).baseValue = 0.19
		getAttribute(FOLLOW_RANGE).baseValue = 44.0
		getAttribute(ENTITY_GRAVITY).baseValue *= 0.725
		
		experienceValue = 1
	}
	
	fun setMaxHealth(maxHealth: Float, resetCurrentHealth: Boolean){
		getAttribute(MAX_HEALTH).baseValue = maxHealth.toDouble()
		
		health = if (resetCurrentHealth)
			maxHealth
		else
			health.coerceAtMost(maxHealth)
	}
	
	override fun registerGoals(){
		aiLeaderEnabledToggle = AIToggle()
		aiLeaderEnabledToggle.enabled = false
		
		aiLeaderDisabledToggle = AIToggle()
		aiLeaderDisabledToggle.enabled = true
		
		itemPickupHandler = BlobbyItemPickupHandler(this)
		
		goalSelector.addGoal(1, AISwim(this))
		goalSelector.addGoal(2, AIWatchDyingLeader(this, ticksBeforeResuming = 49), aiLeaderDisabledToggle)
		goalSelector.addGoal(3, AIPickUpItemDetour(this, chancePerTick = 100, maxDetourTicks = 65..200, searchRadius = 8.0, speedMp = 1.25, handler = itemPickupHandler))
		goalSelector.addGoal(4, AIWanderLand(this, movementSpeed = 1.0, chancePerTick = 45, maxDistanceXZ = 22, maxDistanceY = 6), aiLeaderEnabledToggle)
		goalSelector.addGoal(4, AIFollowLeaderJumping(this), aiLeaderDisabledToggle)
		goalSelector.addGoal(5, AIWatchIdleJumping(this, chancePerTick = 0.07F, delayTicks = 13))
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	// Leadership
	
	private fun setLeaderStatusAndRefresh(isLeader: Boolean){
		isGroupLeader = isLeader
		aiLeaderEnabledToggle.enabled = isLeader
		aiLeaderDisabledToggle.enabled = !isLeader
	}
	
	private fun recreateGroup(){
		val originalGroup = world
			.selectExistingEntities
			.inRange<EntityMobBlobby>(posVec, getAttribute(FOLLOW_RANGE).value)
			.filter { it.groupId == groupId }
			.ifEmpty { listOf(this) }
		
		val newGroupLimit = rand.nextInt(1, rand.nextInt(1, originalGroup.size))
		val newGroups = Array(newGroupLimit){ mutableListOf<EntityMobBlobby>() }
		
		for(peer in originalGroup){
			rand.nextItem(newGroups).add(peer)
		}
		
		for(group in newGroups){
			if (group.isEmpty()){
				continue
			}
			
			val newId = rand.nextLong()
			
			for(blobby in group){
				blobby.groupId = newId
			}
			
			group.maxBy { it.scale }?.setLeaderStatusAndRefresh(isLeader = true)
		}
	}
	
	fun findLeader(): EntityMobBlobby?{
		val maxDist = getAttribute(FOLLOW_RANGE).value
		val checkArea = boundingBox.grow(maxDist, maxDist * 0.5, maxDist)
		
		return world.selectEntities.inBox<EntityMobBlobby>(checkArea).firstOrNull { it.groupId == groupId && it.isGroupLeader && it !== this }
	}
	
	// Behavior
	
	override fun livingTick(){
		super.livingTick()
		
		if (world.isRemote){
			if (wasOnGround && !onGround){
				squishTarget = 0.175F
			}
			else if (!wasOnGround && onGround){
				squishTarget = -0.15F
			}
			
			renderSquishClient.update(offsetTowards(renderSquishClient.currentValue, squishTarget, 0.6F))
			squishTarget *= 0.75F
		}
		else{
			itemPickupHandler.update()
			
			if (ticksExisted % 20 == 0 && heldItem.let { it.item is ItemSword || it.useAction == SPEAR }){
				val dangerousItemName = heldItem.item.registryName!!.toString()
				
				for(blobby in world.selectEntities.inRange<EntityMobBlobby>(posVec, 12.0)){
					if (blobby === this || blobby.canEntityBeSeen(this)){
						blobby.itemPickupHandler.banItem(dangerousItemName)
					}
				}
				
				attackEntityFrom(DamageSource.GENERIC, 1F)
			}
			
			if (ticksExisted % 10 == 0){
				if (isGroupLeader){
					idleTime = if (isFollowRangeAreaLoaded) 0 else 1000
				}
				else{
					idleTime = 0
					
					if (findLeader() == null && goalSelector.runningGoals.noneMatch { it.goal is AIWatchDyingLeader } && isFollowRangeAreaLoaded){
						recreateGroup()
					}
				}
			}
		}
		
		wasOnGround = onGround
	}
	
	override fun applyEntityCollision(other: Entity){
		if (other is EntityMobBlobby){
			isColliding = true
			other.isColliding = true
			
			super.applyEntityCollision(other)
			
			other.isColliding = false
			isColliding = false
		}
		else{
			super.applyEntityCollision(other)
		}
	}
	
	override fun addVelocity(x: Double, y: Double, z: Double){
		if (isColliding && isGroupLeader){
			return
		}
		
		super.addVelocity(x, y, z)
	}
	
	// Movement
	
	override fun createNavigator(world: World): PathNavigator{
		return PathNavigateGroundPreferBeeLine(this, world, maxStuckTicks = 55, fallbackPathfindingResetTicks = 70..110)
	}
	
	override fun createBodyController(): BodyController{
		return EntityBodyHeadOnly(this)
	}
	
	override fun setAIMoveSpeed(speed: Float){
		super.setAIMoveSpeed(if (isInWater) speed * 1.175F else speed * jumpSpeedMp)
	}
	
	override fun getHorizontalFaceSpeed(): Int{
		return 15
	}
	
	override fun getFaceRotSpeed(): Int{
		return 15
	}
	
	private fun getJumpDelay(): Int{
		val delayReduction = when{
			isGroupLeader                           -> 0
			itemPickupHandler.catchUpBonusTicks > 0 -> 9
			else                                    -> 4
		}
		
		return (rand.nextInt(jumpDelay) - delayReduction).coerceAtLeast(5)
	}
	
	override fun getJumpUpwardsMotion(): Float{
		return 0.42F * jumpHeightMp * super.getJumpUpwardsMotion()
	}
	
	override fun jump(){
		val baseMotion = jumpUpwardsMotion.toDouble()
		val lookVec = Vec3.fromYaw(rotationYawHead)
		
		val upwardsMotion = if (aiMoveSpeed > 0.1F && (collidedHorizontally || Pos(posVec.addY(height * 0.5).add(lookVec)).let { it.blocksMovement(world) && !it.up().blocksMovement(world) }))
			0.36 + (baseMotion * 0.15)
		else
			baseMotion
		
		motion = motion.withY(upwardsMotion + ((getActivePotionEffect(JUMP_BOOST)?.let { it.amplifier + 1 } ?: 0) * 0.1)).add(lookVec.scale(aiMoveSpeed))
		isAirBorne = true
	}
	
	// Death
	
	override fun dropInventory(){
		super.dropInventory()
		
		if (heldItem.isNotEmpty){
			entityDropItem(heldItem)
		}
	}
	
	// Spawning
	
	override fun onInitialSpawn(world: IWorld, difficulty: DifficultyInstance, reason: SpawnReason, data: ILivingEntityData?, nbt: CompoundNBT?): ILivingEntityData?{
		val rand = world.random
		val wrld = world.world
		val group = (data as? SpawnGroupData) ?: SpawnGroupData(rand)
		
		group.setupLeader(this)
		
		if (reason == SPAWN_EGG){
			val probableSpawningPlayer = world
				.selectExistingEntities
				.inRange<EntityPlayer>(posVec, 10.0)
				.filter { it.getHeldItem(MAIN_HAND).item === ModItems.SPAWN_BLOBBY || it.getHeldItem(OFF_HAND).item === ModItems.SPAWN_BLOBBY }
				.minBy(::getDistanceSq)
			
			if (probableSpawningPlayer == null || !probableSpawningPlayer.isSneaking){
				repeat(rand.nextInt(1, 7)){
					group.generateChild(wrld, it).apply {
						copyLocationAndAnglesFrom(this@EntityMobBlobby)
						posVec = posVec.add(rand.nextVector2(xz = 0.01, y = 0.0))
						world.addEntity(this)
					}
				}
			}
		}
		
		return super.onInitialSpawn(world, difficulty, reason, group, nbt)
	}
	
	// Despawning
	
	override fun checkDespawn(){}
	
	// Properties
	
	override fun getLootTable(): ResourceLocation{
		return LOOT_TABLE
	}
	
	override fun getExperiencePoints(player: EntityPlayer): Int{
		return rand.nextInt(0, experienceValue)
	}
	
	override fun getStandingEyeHeight(pose: Pose, size: EntitySize): Float{
		return size.height * 0.575F
	}
	
	override fun getSoundVolume(): Float{
		return 0.5F
	}
	
	override fun getSoundPitch(): Float{
		return 1.5F
	}
	
	override fun getHurtSound(source: DamageSource): SoundEvent{
		return Sounds.ENTITY_SLIME_HURT_SMALL
	}
	
	override fun getDeathSound(): SoundEvent{
		return Sounds.ENTITY_SLIME_DEATH_SMALL
	}
	
	override fun isInRangeToRenderDist(distanceSq: Double): Boolean{
		return distanceSq < square(96.0)
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		putLong(GROUP_ID_TAG, groupId)
		putBoolean(GROUP_LEADER_TAG, isGroupLeader)
		
		putFloat(MAX_HEALTH_TAG, maxHealth)
		putFloat(SCALE_TAG, scale)
		putInt(COLOR_TAG, color.i)
		
		putByte(JUMP_DELAY_MIN_TAG, jumpDelay.first.toByte())
		putByte(JUMP_DELAY_MAX_TAG, jumpDelay.last.toByte())
		putFloat(JUMP_SPEED_TAG, jumpSpeedMp)
		putFloat(JUMP_HEIGHT_TAG, jumpHeightMp)
		
		putStack(HELD_ITEM_TAG, heldItem)
		put(ITEM_PICKUP_TAG, itemPickupHandler.serializeNBT())
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		groupId = getLong(GROUP_ID_TAG)
		setLeaderStatusAndRefresh(getBoolean(GROUP_LEADER_TAG))
		
		setMaxHealth(getFloatOrNull(MAX_HEALTH_TAG) ?: maxHealth, resetCurrentHealth = false)
		scale = getFloatOrNull(SCALE_TAG) ?: scale
		color = IntColor(getIntegerOrNull(COLOR_TAG) ?: color.i)
		
		jumpDelay = getByte(JUMP_DELAY_MIN_TAG)..getByte(JUMP_DELAY_MAX_TAG)
		jumpSpeedMp = getFloat(JUMP_SPEED_TAG)
		jumpHeightMp = getFloat(JUMP_HEIGHT_TAG)
		
		heldItem = getStack(HELD_ITEM_TAG)
		itemPickupHandler.deserializeNBT(getCompound(ITEM_PICKUP_TAG))
	}
}
