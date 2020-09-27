package chylex.hee.game.entity.living
import chylex.hee.game.entity.living.ai.AttackMelee
import chylex.hee.game.entity.living.ai.Swim
import chylex.hee.game.entity.living.ai.TargetAttacker
import chylex.hee.game.entity.living.ai.TargetNearby
import chylex.hee.game.entity.living.ai.WanderLand
import chylex.hee.game.entity.living.ai.WatchIdle
import chylex.hee.game.entity.motionY
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS_WITH_SHIELD
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.system.MagicValues.DEATH_TIME_MAX
import chylex.hee.system.facades.Resource
import chylex.hee.system.math.square
import chylex.hee.system.migration.EntityMob
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Sounds
import chylex.hee.system.random.nextFloat
import net.minecraft.block.BlockState
import net.minecraft.entity.CreatureAttribute
import net.minecraft.entity.Entity
import net.minecraft.entity.EntitySize
import net.minecraft.entity.EntityType
import net.minecraft.entity.Pose
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.network.IPacket
import net.minecraft.util.DamageSource
import net.minecraft.util.Hand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks
import kotlin.math.abs
import kotlin.math.max

class EntityMobUndread(type: EntityType<EntityMobUndread>, world: World) : EntityMob(type, world){
	@Suppress("unused")
	constructor(world: World) : this(ModEntities.UNDREAD, world)
	
	private companion object{
		private val DAMAGE_GENERAL = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS_WITH_SHIELD)
	}
	
	override fun registerAttributes(){
		super.registerAttributes()
		
		getAttribute(MAX_HEALTH).baseValue = 12.0
		getAttribute(ATTACK_DAMAGE).baseValue = 4.0
		getAttribute(MOVEMENT_SPEED).baseValue = 0.18
		getAttribute(FOLLOW_RANGE).baseValue = 24.0
		
		experienceValue = 5
	}
	
	override fun registerGoals(){
		goalSelector.addGoal(1, Swim(this))
		goalSelector.addGoal(2, AttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = true))
		goalSelector.addGoal(3, WanderLand(this, movementSpeed = 0.9, chancePerTick = 12, maxDistanceXZ = 7, maxDistanceY = 3))
		goalSelector.addGoal(4, WatchIdle(this))
		
		targetSelector.addGoal(1, TargetAttacker(this, callReinforcements = false))
		targetSelector.addGoal(2, TargetNearby(this, chancePerTick = 1, checkSight = false, easilyReachableOnly = false, targetPredicate = ::isPlayerNearby ))
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	private fun isPlayerNearby(player: EntityPlayer): Boolean{
		return abs(posY - player.posY) <= 3 && getDistanceSq(player) < square(16)
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean{
		return DAMAGE_GENERAL.dealToFrom(entity, this)
	}
	
	override fun getLootTable(): ResourceLocation{
		return Resource.Custom("entities/undread")
	}
	
	override fun getCreatureAttribute(): CreatureAttribute{
		return CreatureAttribute.UNDEAD
	}
	
	override fun getStandingEyeHeight(pose: Pose, size: EntitySize): Float{
		return 1.68F
	}
	
	override fun swingArm(hand: Hand){}
	
	override fun onDeathUpdate(){
		noClip = true
		motionY = max(0.05, motionY)
		
		if (deathTime >= (DEATH_TIME_MAX / 2) + 1){
			deathTime = DEATH_TIME_MAX - 1
		}
		
		super.onDeathUpdate()
	}
	
	override fun playStepSound(pos: BlockPos, state: BlockState){
		playSound(Sounds.ENTITY_ZOMBIE_STEP, rand.nextFloat(0.4F, 0.5F), rand.nextFloat(0.9F, 1F))
	}
	
	override fun getHurtSound(source: DamageSource): SoundEvent{
		return ModSounds.MOB_UNDREAD_HURT
	}
	
	override fun getDeathSound(): SoundEvent{
		return ModSounds.MOB_UNDREAD_DEATH
	}
	
	override fun getSoundPitch(): Float{
		return rand.nextFloat(0.8F, 1F)
	}
}
