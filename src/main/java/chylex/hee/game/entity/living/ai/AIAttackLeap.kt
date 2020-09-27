package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.motionX
import chylex.hee.game.entity.motionZ
import chylex.hee.game.world.totalTime
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.addXZ
import chylex.hee.system.math.directionTowards
import chylex.hee.system.math.square
import chylex.hee.system.math.withY
import chylex.hee.system.migration.EntityCreature
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.random.nextFloat
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.goal.Goal.Flag.JUMP
import net.minecraft.entity.ai.goal.Goal.Flag.MOVE
import java.util.EnumSet

class AIAttackLeap(
	private val entity: EntityCreature,
	triggerDistance: ClosedFloatingPointRange<Double>,
	private val triggerChance: Float,
	private val triggerCooldown: Int,
	private val leapStrengthXZ: ClosedFloatingPointRange<Double>,
	private val leapStrengthY: ClosedFloatingPointRange<Double>
) : Goal(){
	private val triggerDistanceSq = square(triggerDistance.start)..square(triggerDistance.endInclusive)
	
	private var leapTarget: EntityLivingBase? = null
	private var lastLeapTime = 0L
	private var hasAttacked = false
	
	init{
		mutexFlags = EnumSet.of(MOVE, JUMP)
	}
	
	override fun shouldExecute(): Boolean{
		if (!entity.onGround){ // also prevents instant jump after taking damage
			return false
		}
		
		val target = entity.attackTarget ?: return false
		val currentTime = entity.world.totalTime
		
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
		
		entity.motion = entity.motion.withY(strengthY).addXZ(
			(diff.x * strengthXZ) + (entity.motionX * 0.1),
			(diff.z * strengthXZ) + (entity.motionZ * 0.1)
		)
		
		lastLeapTime = entity.world.totalTime
	}
	
	override fun tick(){
		if (hasAttacked){
			return
		}
		
		val target = leapTarget ?: return
		
		val distSq = entity.getDistanceSq(target.posX, target.boundingBox.maxY, target.posZ)
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
