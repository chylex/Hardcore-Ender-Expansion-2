package chylex.hee.game.entity.living
import chylex.hee.game.entity.living.ai.AIWanderLand
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS_WITH_SHIELD
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.init.ModLoot
import chylex.hee.init.ModSounds
import chylex.hee.system.migration.MagicValues.DEATH_TIME_MAX
import chylex.hee.system.util.AIAttackMelee
import chylex.hee.system.util.AISwim
import chylex.hee.system.util.AITargetAttacker
import chylex.hee.system.util.AITargetNearby
import chylex.hee.system.util.AIWatchIdle
import chylex.hee.system.util.getAttribute
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.square
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.EnumCreatureAttribute
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.abs
import kotlin.math.max

class EntityMobUndread(world: World) : EntityMob(world){
	private companion object{
		private val DAMAGE_GENERAL = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS_WITH_SHIELD)
	}
	
	init{
		setSize(0.625F, 1.925F)
	}
	
	override fun applyEntityAttributes(){
		super.applyEntityAttributes()
		
		getAttribute(MAX_HEALTH).baseValue = 12.0
		getAttribute(ATTACK_DAMAGE).baseValue = 4.0
		getAttribute(MOVEMENT_SPEED).baseValue = 0.18
		getAttribute(FOLLOW_RANGE).baseValue = 24.0
		
		experienceValue = 5
	}
	
	override fun initEntityAI(){
		tasks.addTask(1, AISwim(this))
		tasks.addTask(2, AIAttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = true))
		tasks.addTask(3, AIWanderLand(this, movementSpeed = 0.9, chancePerTick = 12, maxDistanceXZ = 7, maxDistanceY = 3))
		tasks.addTask(4, AIWatchIdle(this))
		
		targetTasks.addTask(1, AITargetAttacker(this, callReinforcements = false))
		targetTasks.addTask(2, AITargetNearby(this, chancePerTick = 1, checkSight = false, easilyReachableOnly = false, targetPredicate = ::isPlayerNearby ))
	}
	
	private fun isPlayerNearby(player: EntityPlayer): Boolean{
		return abs(posY - player.posY) <= 3 && getDistanceSq(player) < square(16)
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean{
		return DAMAGE_GENERAL.dealToFrom(entity, this)
	}
	
	override fun getLootTable(): ResourceLocation{
		return ModLoot.UNDREAD
	}
	
	override fun getCreatureAttribute(): EnumCreatureAttribute{
		return EnumCreatureAttribute.UNDEAD
	}
	
	override fun getEyeHeight(): Float{
		return 1.68F
	}
	
	override fun swingArm(hand: EnumHand){}
	
	override fun onDeathUpdate(){
		noClip = true
		motionY = max(0.05, motionY)
		
		if (deathTime >= (DEATH_TIME_MAX / 2) + 1){
			deathTime = DEATH_TIME_MAX - 1
		}
		
		super.onDeathUpdate()
	}
	
	override fun playStepSound(pos: BlockPos, block: Block){
		playSound(SoundEvents.ENTITY_ZOMBIE_STEP, rand.nextFloat(0.4F, 0.5F), rand.nextFloat(0.9F, 1F))
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
