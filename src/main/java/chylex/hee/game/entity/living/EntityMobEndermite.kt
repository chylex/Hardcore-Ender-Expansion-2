package chylex.hee.game.entity.living
import chylex.hee.HEE
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.living.ai.AIForceWanderTiming
import chylex.hee.game.entity.living.ai.AIWanderLand
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.init.ModLoot
import chylex.hee.system.util.AIAttackMelee
import chylex.hee.system.util.AISwim
import chylex.hee.system.util.AITargetAttacker
import chylex.hee.system.util.AITargetNearby
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.square
import net.minecraft.entity.Entity
import net.minecraft.entity.EnumCreatureAttribute
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.monster.EntityEndermite
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World

open class EntityMobEndermite(world: World) : EntityEndermite(world){
	private companion object{
		private val DAMAGE_GENERAL = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS)
	}
	
	private var realLifetime = 0
	private var idleDespawnTimer: Short = 0
	
	init{
		setSize(0.425F, 0.325F)
	}
	
	override fun applyEntityAttributes(){
		super.applyEntityAttributes()
		
		getEntityAttribute(MAX_HEALTH).baseValue = 8.0
		getEntityAttribute(ATTACK_DAMAGE).baseValue = 2.0
		
		experienceValue = 3
	}
	
	override fun initEntityAI(){
		val aiWander = AIWanderLand(this, movementSpeed = 1.0, chancePerTick = 50)
		
		tasks.addTask(1, AISwim(this))
		tasks.addTask(2, AIAttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = false))
		tasks.addTask(3, aiWander)
		tasks.addTask(4, AIForceWanderTiming(this, aiWander, defaultChancePerTick = 50, forcedTimingRange = 10..74))
		// no watching AI because it makes Endermites spazz out
		
		targetTasks.addTask(1, AITargetAttacker(this, callReinforcements = true))
		targetTasks.addTask(2, AITargetNearby<EntityPlayer>(this, chancePerTick = 10, checkSight = true, easilyReachableOnly = false))
	}
	
	override fun onLivingUpdate(){
		idleTime = 0
		lifetime = 0
		++realLifetime
		
		with(HEE.proxy){
			pauseParticles()
			super.onLivingUpdate()
			resumeParticles()
		}
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean{
		return DAMAGE_GENERAL.dealToFrom(entity, this)
	}
	
	override fun getLootTable(): ResourceLocation{
		return ModLoot.ENDERMITE_NATURAL
	}
	
	override fun getCreatureAttribute(): EnumCreatureAttribute{
		return CustomCreatureType.ENDER
	}
	
	override fun getCanSpawnHere(): Boolean{
		return true
	}
	
	override fun despawnEntity(){
		if (isNoDespawnRequired || !canDespawn()){
			return
		}
		
		val closest = world.getClosestPlayerToEntity(this, -1.0)
		
		if (closest == null){
			return
		}
		
		val distance = closest.getDistanceSq(this)
		
		if (distance > square(128)){
			setDead()
		}
		else if (distance > square(32)){
			if (++idleDespawnTimer >= 900 || (realLifetime % 20 == 0 && rng.nextInt(50) == 0)){
				setDead()
			}
		}
		else{
			idleDespawnTimer = 0
		}
	}
	
	override fun canDespawn(): Boolean{
		return realLifetime > 1800
	}
	
	override fun writeEntityToNBT(nbt: TagCompound) = with(nbt.heeTag){
		super.writeEntityToNBT(nbt)
		
		setInteger("Age", realLifetime)
		setShort("Idle", idleDespawnTimer)
	}
	
	override fun readEntityFromNBT(nbt: TagCompound) = with(nbt.heeTag){
		super.readEntityFromNBT(nbt)
		
		realLifetime = getInteger("Age")
		idleDespawnTimer = getShort("Idle")
	}
}
