package chylex.hee.game.entity.living
import chylex.hee.HEE
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.living.ai.AIForceWanderTiming
import chylex.hee.game.entity.living.ai.AIWanderLand
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.init.ModEntities
import chylex.hee.system.migration.vanilla.EntityEndermite
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.AIAttackMelee
import chylex.hee.system.util.AISwim
import chylex.hee.system.util.AITargetAttacker
import chylex.hee.system.util.AITargetNearby
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import net.minecraft.entity.CreatureAttribute
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.network.IPacket
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks

open class EntityMobEndermite(type: EntityType<out EntityMobEndermite>, world: World) : EntityEndermite(type, world){
	constructor(world: World) : this(ModEntities.ENDERMITE, world)
	
	private companion object{
		private val DAMAGE_GENERAL = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS)
		
		private const val AGE_TAG = "Age"
		private const val IDLE_TAG = "Idle"
	}
	
	private var realLifetime = 0
	private var idleDespawnTimer: Short = 0
	
	override fun registerAttributes(){
		super.registerAttributes()
		
		getAttribute(MAX_HEALTH).baseValue = 8.0
		getAttribute(ATTACK_DAMAGE).baseValue = 2.0
		
		experienceValue = 3
	}
	
	override fun registerGoals(){
		val aiWander = AIWanderLand(this, movementSpeed = 1.0, chancePerTick = 50)
		
		goalSelector.addGoal(1, AISwim(this))
		goalSelector.addGoal(2, AIAttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = false))
		goalSelector.addGoal(3, aiWander)
		goalSelector.addGoal(4, AIForceWanderTiming(this, aiWander, defaultChancePerTick = 50, forcedTimingRange = 10..74))
		// no watching AI because it makes Endermites spazz out
		
		targetSelector.addGoal(1, AITargetAttacker(this, callReinforcements = true))
		targetSelector.addGoal(2, AITargetNearby<EntityPlayer>(this, chancePerTick = 10, checkSight = true, easilyReachableOnly = false))
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun livingTick(){
		idleTime = 0
		lifetime = 0
		++realLifetime
		
		with(HEE.proxy){
			pauseParticles()
			super.livingTick()
			resumeParticles()
		}
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean{
		return DAMAGE_GENERAL.dealToFrom(entity, this)
	}
	
	override fun getLootTable(): ResourceLocation{
		return Resource.Custom("entities/endermite_natural")
	}
	
	override fun getCreatureAttribute(): CreatureAttribute{
		return CustomCreatureType.ENDER
	}
	
	override fun checkDespawn(){
		if (isNoDespawnRequired || preventDespawn()){
			return
		}
		
		val closest = world.getClosestPlayer(this, -1.0)
		
		if (closest == null){
			return
		}
		
		val distance = closest.getDistanceSq(this)
		
		if (distance > square(128)){
			remove()
		}
		else if (distance > square(32)){
			if (++idleDespawnTimer >= 900 || (realLifetime % 20 == 0 && rng.nextInt(50) == 0)){
				remove()
			}
		}
		else{
			idleDespawnTimer = 0
		}
	}
	
	override fun canDespawn(distanceToClosestPlayerSq: Double): Boolean{
		return realLifetime > 1800
	}
	
	override fun preventDespawn(): Boolean{
		return super.preventDespawn() || realLifetime <= 1800
	}
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		putInt(AGE_TAG, realLifetime)
		putShort(IDLE_TAG, idleDespawnTimer)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		realLifetime = getInt(AGE_TAG)
		idleDespawnTimer = getShort(IDLE_TAG)
	}
}
