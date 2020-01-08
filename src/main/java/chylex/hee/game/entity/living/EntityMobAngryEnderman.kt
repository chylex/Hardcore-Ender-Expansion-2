package chylex.hee.game.entity.living
import chylex.hee.HEE
import chylex.hee.game.entity.living.ai.AIWanderLand
import chylex.hee.game.entity.living.behavior.EndermanTeleportHandler
import chylex.hee.game.entity.living.behavior.EndermanWaterHandler
import chylex.hee.init.ModEntities
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.AIAttackMelee
import chylex.hee.system.util.AISwim
import chylex.hee.system.util.AITargetAttacker
import chylex.hee.system.util.AITargetNearby
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import net.minecraft.entity.EntityPredicate
import net.minecraft.entity.EntityType
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceContext.BlockMode
import net.minecraft.util.math.RayTraceContext.FluidMode
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class EntityMobAngryEnderman(type: EntityType<EntityMobAngryEnderman>, world: World) : EntityMobAbstractEnderman(type, world){
	constructor(world: World) : this(ModEntities.ANGRY_ENDERMAN, world)
	
	private companion object{
		private const val TELEPORT_HANDLER_TAG = "Teleport"
		private const val WATER_HANDLER_TAG = "Water"
		private const val DESPAWN_COOLDOWN_TAG = "DespawnCooldown"
		
		private const val AGGRO_DISTANCE = 12
		private const val AGGRO_DISTANCE_SQ = AGGRO_DISTANCE * AGGRO_DISTANCE
	}
	
	private lateinit var teleportHandler: EndermanTeleportHandler
	private lateinit var waterHandler: EndermanWaterHandler
	
	override val teleportCooldown = 35
	private var despawnCooldown = 300
	
	override fun registerAttributes(){
		super.registerAttributes()
		
		getAttribute(MAX_HEALTH).baseValue = 40.0
		getAttribute(ATTACK_DAMAGE).baseValue = 7.0
		getAttribute(MOVEMENT_SPEED).baseValue = 0.315
		getAttribute(FOLLOW_RANGE).baseValue = 32.0
		
		experienceValue = 7
	}
	
	override fun registerGoals(){
		teleportHandler = EndermanTeleportHandler(this)
		waterHandler = EndermanWaterHandler(this, takeDamageAfterWetTicks = Int.MAX_VALUE)
		
		goalSelector.addGoal(1, AISwim(this))
		goalSelector.addGoal(2, AIAttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = true))
		goalSelector.addGoal(3, AIWanderLand(this, movementSpeed = 0.8, chancePerTick = 90))
		
		targetSelector.addGoal(1, AITargetAttacker(this, callReinforcements = false))
		targetSelector.addGoal(2, AITargetNearby<EntityPlayer>(this, chancePerTick = 1, checkSight = false, easilyReachableOnly = false){ getDistanceSq(it) < AGGRO_DISTANCE_SQ })
	}
	
	override fun livingTick(){
		with(HEE.proxy){
			pauseParticles()
			super.livingTick()
			resumeParticles()
		}
	}
	
	override fun updateAITasks(){
		teleportHandler.update()
		waterHandler.update()
		
		val currentTarget = attackTarget
		
		if (currentTarget != null){
			val distanceSq = getDistanceSq(currentTarget)
			
			if (distanceSq > square(getAttribute(FOLLOW_RANGE).value * 0.75) && !entitySenses.canSee(currentTarget)){
				attackTarget = null
			}
			else if (distanceSq > AGGRO_DISTANCE_SQ){
				val predicate = EntityPredicate().setLineOfSiteRequired()
				
				val alternativeTarget = world
					.selectVulnerableEntities
					.inRange<EntityPlayer>(posVec, AGGRO_DISTANCE.toDouble())
					.filter { predicate.canTarget(this, it) }
					.minBy(::getDistanceSq)
				
				if (alternativeTarget != null){
					attackTarget = alternativeTarget
				}
				else if (teleportHandler.checkCooldownSilent()){
					teleportHandler.teleportTowards(currentTarget, -40F..40F, (7.5)..(9.0))
				}
			}
		}
	}
	
	override fun canTeleportTo(aabb: AxisAlignedBB): Boolean{
		if (!super.canTeleportTo(aabb)){
			return false
		}
		
		val currentTarget = attackTarget ?: return false
		val teleportPos = Vec3d(aabb.minX + (aabb.maxX - aabb.minX) * 0.5, aabb.minY + eyeHeight.toDouble(), aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5)
		
		return world.rayTraceBlocks(RayTraceContext(teleportPos, currentTarget.lookPosVec, BlockMode.COLLIDER, FluidMode.NONE, this)).type == Type.MISS
	}
	
	override fun getLootTable(): ResourceLocation{
		return Resource.Custom("entities/angry_enderman")
	}
	
	override fun canDespawn(distanceToClosestPlayerSq: Double): Boolean{
		return despawnCooldown == 0 && distanceToClosestPlayerSq > square(128.0)
	}
	
	override fun preventDespawn(): Boolean{
		return despawnCooldown > 0
	}
	
	override fun checkDespawn(){
		if (despawnCooldown > 0){
			--despawnCooldown
		}
		
		if (!isNoDespawnRequired && rand.nextInt(600) == 0){
			val closestPlayer = world.getClosestPlayer(this, -1.0)
			
			if (closestPlayer != null && canDespawn(getDistanceSq(closestPlayer))){
				remove()
			}
		}
		else{
			idleTime = 0
		}
	}
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		put(TELEPORT_HANDLER_TAG, teleportHandler.serializeNBT())
		put(WATER_HANDLER_TAG, waterHandler.serializeNBT())
		
		putInt(DESPAWN_COOLDOWN_TAG, despawnCooldown)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		teleportHandler.deserializeNBT(getCompound(TELEPORT_HANDLER_TAG))
		waterHandler.deserializeNBT(getCompound(WATER_HANDLER_TAG))
		
		despawnCooldown = getInt(DESPAWN_COOLDOWN_TAG)
	}
}
