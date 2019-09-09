package chylex.hee.game.entity.living.ai
import chylex.hee.system.util.AI_FLAG_MOVEMENT
import chylex.hee.system.util.AI_FLAG_SWIMMING
import chylex.hee.system.util.Vec3
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.square
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.ai.EntityAIBase
import net.minecraft.util.EnumHand.MAIN_HAND

class AIAttackLeap(
	private val entity: EntityCreature,
	triggerDistance: ClosedFloatingPointRange<Double>,
	private val triggerChance: Float,
	private val triggerCooldown: Int,
	private val leapStrengthXZ: ClosedFloatingPointRange<Double>,
	private val leapStrengthY: ClosedFloatingPointRange<Double>
) : EntityAIBase(){
	private val triggerDistanceSq = square(triggerDistance.start)..square(triggerDistance.endInclusive)
	
	private var leapTarget: EntityLivingBase? = null
	private var lastLeapTime = 0L
	private var hasAttacked = false
	
	init{
		mutexBits = AI_FLAG_SWIMMING or AI_FLAG_MOVEMENT
	}
	
	override fun shouldExecute(): Boolean{
		if (!entity.onGround){ // also prevents instant jump after taking damage
			return false
		}
		
		val target = entity.attackTarget ?: return false
		val currentTime = entity.world.totalWorldTime
		
		if (currentTime - lastLeapTime < triggerCooldown){
			return false
		}
		
		if (entity.getDistanceSq(target) !in triggerDistanceSq){
			return false
		}
		
		if (entity.rng.nextFloat() >= triggerChance){
			lastLeapTime = currentTime
			return false
		}
		
		leapTarget = target
		return true
	}
	
	override fun shouldContinueExecuting(): Boolean{
		return !entity.onGround
	}
	
	override fun startExecuting(){
		val target = leapTarget ?: return
		val diff = Vec3.fromXZ(entity.posX, entity.posZ).directionTowards(Vec3.fromXZ(target.posX, target.posZ))
		
		val rand = entity.rng
		val strengthXZ = rand.nextFloat(leapStrengthXZ)
		val strengthY = rand.nextFloat(leapStrengthY)
		
		entity.motionX += (diff.x * strengthXZ) + (entity.motionX * 0.1)
		entity.motionZ += (diff.z * strengthXZ) + (entity.motionZ * 0.1)
		entity.motionY = strengthY
		
		lastLeapTime = entity.world.totalWorldTime
	}
	
	override fun updateTask(){
		if (hasAttacked){
			return
		}
		
		val target = leapTarget ?: return
		
		val distSq = entity.getDistanceSq(target.posX, target.entityBoundingBox.maxY, target.posZ)
		val reachSq = square(entity.width) + (target.width * 0.5F)
		
		if (distSq <= reachSq){
			entity.swingArm(MAIN_HAND)
			entity.attackEntityAsMob(target)
			hasAttacked = true
		}
	}
	
	override fun resetTask(){
		leapTarget = null
		hasAttacked = false
	}
}
