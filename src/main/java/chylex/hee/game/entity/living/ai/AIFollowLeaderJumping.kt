package chylex.hee.game.entity.living.ai
import chylex.hee.game.entity.living.EntityMobBlobby
import chylex.hee.game.entity.lookDirVec
import chylex.hee.system.math.offsetTowards
import chylex.hee.system.math.scale
import chylex.hee.system.math.square
import chylex.hee.system.math.toRadians
import chylex.hee.system.math.withY
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextVector2
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.goal.Goal.Flag.JUMP
import net.minecraft.entity.ai.goal.Goal.Flag.MOVE
import net.minecraft.util.math.vector.Vector3d
import java.util.EnumSet
import kotlin.math.abs

class AIFollowLeaderJumping(private val entity: EntityMobBlobby) : Goal(){
	private var leader: EntityMobBlobby? = null
	
	private var offset = Vector3d.ZERO
	private var targetOffset = Vector3d.ZERO
	private var offsetChangeTicks = 0
	private var stoppedTicks = 0
	
	init{
		mutexFlags = EnumSet.of(MOVE, JUMP)
	}
	
	override fun shouldExecute(): Boolean{
		val leader = this.leader ?: entity.findLeader().also { this.leader = it } ?: return false
		val distSq = entity.getDistanceSq(leader.posX + offset.x, leader.posY + offset.y, leader.posZ + offset.z)
		
		return distSq > square(2.0) || (distSq > square(0.8) && entity.motion.withY(0.0).lengthSquared() > square(0.05))
	}
	
	override fun shouldContinueExecuting(): Boolean{
		return stoppedTicks < 10 && leader.let { it == null || it.deathTime == 0 }
	}
	
	override fun tick(){
		val leader = this.leader ?: return
		
		if (moveTo(leader.posX + offset.x, leader.posY + offset.y, leader.posZ + offset.z)){
			val rand = entity.rng
			
			if (++offsetChangeTicks > rand.nextInt(17, 300)){
				offsetChangeTicks = 0
				
				val baseDist = entity.width + (rand.nextFloat(1.8F, 2.5F) * leader.width)
				val revLook = leader.lookDirVec.inverse()
				val yawRot = rand.nextFloat(15F, rand.nextFloat(90F, 110F)) * (if (rand.nextBoolean()) 1F else -1F)
				val yawVec = revLook.rotateYaw(yawRot.toRadians())
				
				targetOffset = rand.nextVector2(xz = baseDist * rand.nextFloat(0.01, 0.29), y = 0.0).add(yawVec.scale(baseDist * (10F + abs(yawRot)) * 0.014F))
			}
			else{
				offset = offset.offsetTowards(targetOffset, 0.005)
			}
			
			stoppedTicks = 0
		}
		else{
			++stoppedTicks
		}
	}
	
	override fun resetTask(){
		leader = null
		stoppedTicks = 0
	}
	
	private fun moveTo(x: Double, y: Double, z: Double): Boolean{
		val distSq = entity.getDistanceSq(x, y, z)
		
		if (distSq < 1.0){
			return false
		}
		
		return entity.navigator.tryMoveToXYZ(x, y, z, 1.0)
	}
}
