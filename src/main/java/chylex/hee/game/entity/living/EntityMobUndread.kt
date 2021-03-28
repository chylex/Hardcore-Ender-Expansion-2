package chylex.hee.game.entity.living

import chylex.hee.game.block.BlockDustyStoneUnstable
import chylex.hee.game.entity.living.ai.AttackMelee
import chylex.hee.game.entity.living.ai.Swim
import chylex.hee.game.entity.living.ai.TargetAttacker
import chylex.hee.game.entity.living.ai.TargetNearby
import chylex.hee.game.entity.living.ai.WanderLand
import chylex.hee.game.entity.living.ai.WatchIdle
import chylex.hee.game.entity.living.behavior.UndreadDustEffects
import chylex.hee.game.entity.living.path.PathNavigateGroundCustomProcessor
import chylex.hee.game.entity.motionY
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS_WITH_SHIELD
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.world.Pos
import chylex.hee.game.world.getBlock
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.system.MagicValues.DEATH_TIME_MAX
import chylex.hee.system.facades.Resource
import chylex.hee.system.math.square
import chylex.hee.system.migration.EntityMob
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Sounds
import chylex.hee.system.random.nextFloat
import chylex.hee.system.serialization.NBTList.Companion.putList
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getListOfStrings
import chylex.hee.system.serialization.heeTag
import chylex.hee.system.serialization.readTag
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writeTag
import net.minecraft.block.BlockState
import net.minecraft.entity.CreatureAttribute
import net.minecraft.entity.Entity
import net.minecraft.entity.EntitySize
import net.minecraft.entity.EntityType
import net.minecraft.entity.ILivingEntityData
import net.minecraft.entity.Pose
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.entity.SpawnReason
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.pathfinding.PathNavigator
import net.minecraft.pathfinding.PathNodeType
import net.minecraft.pathfinding.WalkNodeProcessor
import net.minecraft.util.DamageSource
import net.minecraft.util.Hand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.network.NetworkHooks
import kotlin.math.abs
import kotlin.math.max

class EntityMobUndread(type: EntityType<EntityMobUndread>, world: World) : EntityMob(type, world), IEntityAdditionalSpawnData {
	@Suppress("unused")
	constructor(world: World) : this(ModEntities.UNDREAD, world)
	
	private companion object {
		private val DAMAGE_GENERAL = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS_WITH_SHIELD)
		
		private const val DUSTS_TAG = "Dusts"
	}
	
	private var dustEffects = UndreadDustEffects.NONE
	
	override fun registerAttributes() {
		super.registerAttributes()
		
		getAttribute(MAX_HEALTH).baseValue = 12.0
		getAttribute(ATTACK_DAMAGE).baseValue = 4.0
		getAttribute(MOVEMENT_SPEED).baseValue = 0.18
		getAttribute(FOLLOW_RANGE).baseValue = 24.0
		
		experienceValue = 5
	}
	
	override fun registerGoals() {
		goalSelector.addGoal(1, Swim(this))
		goalSelector.addGoal(2, AttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = true))
		goalSelector.addGoal(3, WanderLand(this, movementSpeed = 0.9, chancePerTick = 12, maxDistanceXZ = 7, maxDistanceY = 3))
		goalSelector.addGoal(4, WatchIdle(this))
		
		targetSelector.addGoal(1, TargetAttacker(this, callReinforcements = false))
		targetSelector.addGoal(2, TargetNearby(this, chancePerTick = 1, checkSight = false, easilyReachableOnly = false, targetPredicate = ::isPlayerNearby))
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun writeSpawnData(buffer: PacketBuffer) = buffer.use {
		writeTag(TagCompound().apply { putList(DUSTS_TAG, dustEffects.serializeNBT()) })
	}
	
	override fun readSpawnData(buffer: PacketBuffer) = buffer.use {
		dustEffects = UndreadDustEffects.fromNBT(readTag().getListOfStrings(DUSTS_TAG))
	}
	
	override fun tick() {
		super.tick()
		
		if (world.isRemote && isAlive) {
			dustEffects.tickClient(this)
		}
	}
	
	private fun isPlayerNearby(player: EntityPlayer): Boolean {
		return abs(posY - player.posY) <= 3 && getDistanceSq(player) < square(16)
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean {
		return dustEffects.onAttack(this) || DAMAGE_GENERAL.dealToFrom(entity, this)
	}
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean {
		val newAmount = dustEffects.onHit(this, amount)
		if (newAmount <= 0F) {
			return false
		}
		
		if (super.attackEntityFrom(source, newAmount)) {
			dustEffects.onHurt(this, source)
			return true
		}
		
		return false
	}
	
	public override fun createRunningParticles() {
		super.createRunningParticles()
	}
	
	override fun createNavigator(world: World): PathNavigator {
		return object : PathNavigateGroundCustomProcessor(this, world) {
			override fun createNodeProcessor() = NodeProcessor()
		}
	}
	
	private class NodeProcessor : WalkNodeProcessor() {
		override fun getPathNodeType(world: IBlockReader, x: Int, y: Int, z: Int): PathNodeType {
			if (entity.attackTarget != null) {
				return super.getPathNodeType(world, x, y, z)
			}
			
			val posBelow = Pos(x, y, z).down()
			
			if (posBelow.getBlock(world) !is BlockDustyStoneUnstable || BlockDustyStoneUnstable.getCrumbleStartPos(world, posBelow) == null) {
				return super.getPathNodeType(world, x, y, z)
			}
			
			return PathNodeType.BLOCKED
		}
	}
	
	override fun getLootTable(): ResourceLocation {
		return Resource.Custom("entities/undread")
	}
	
	override fun getCreatureAttribute(): CreatureAttribute {
		return CreatureAttribute.UNDEAD
	}
	
	override fun getStandingEyeHeight(pose: Pose, size: EntitySize): Float {
		return 1.68F
	}
	
	override fun swingArm(hand: Hand) {}
	
	override fun onInitialSpawn(world: IWorld, difficulty: DifficultyInstance, reason: SpawnReason, data: ILivingEntityData?, nbt: CompoundNBT?): ILivingEntityData? {
		if (data is UndreadDustEffects) {
			this.dustEffects = data.also { it.applyAttributes(this) }
		}
		
		return super.onInitialSpawn(world, difficulty, reason, data, nbt)
	}
	
	override fun onDeathUpdate() {
		noClip = true
		motionY = max(0.05, motionY)
		
		if (deathTime >= (DEATH_TIME_MAX / 2) + 1) {
			deathTime = DEATH_TIME_MAX - 1
		}
		
		super.onDeathUpdate()
	}
	
	public override fun playStepSound(pos: BlockPos, state: BlockState) {
		playSound(Sounds.ENTITY_ZOMBIE_STEP, rand.nextFloat(0.4F, 0.5F), rand.nextFloat(0.9F, 1F))
	}
	
	public override fun getHurtSound(source: DamageSource): SoundEvent {
		return ModSounds.MOB_UNDREAD_HURT
	}
	
	public override fun getDeathSound(): SoundEvent {
		return ModSounds.MOB_UNDREAD_DEATH
	}
	
	public override fun getSoundPitch(): Float {
		return rand.nextFloat(0.8F, 1F)
	}
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		putList(DUSTS_TAG, dustEffects.serializeNBT())
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		dustEffects = UndreadDustEffects.fromNBT(getListOfStrings(DUSTS_TAG))
	}
}
