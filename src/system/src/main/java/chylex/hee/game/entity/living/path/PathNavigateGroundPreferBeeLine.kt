package chylex.hee.game.entity.living.path
import chylex.hee.game.entity.posVec
import chylex.hee.system.math.Vec
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.square
import chylex.hee.system.migration.EntityLiving
import chylex.hee.system.random.nextInt
import net.minecraft.entity.Entity
import net.minecraft.pathfinding.GroundPathNavigator
import net.minecraft.world.World

class PathNavigateGroundPreferBeeLine(entity: EntityLiving, world: World, private val maxStuckTicks: Int, private val fallbackPathfindingResetTicks: IntRange) : GroundPathNavigator(entity, world){
	private var beelineTarget = Vec3.ZERO
	private var beelineSpeed = 0.0
	
	private var lastX = entity.posX
	private var lastZ = entity.posZ
	
	private var stuckTicks = 0
	private var stuckLongTicks = 0
	private var resetTicks = 0
	
	val isBeelining
		get() = beelineSpeed > 0.0
	
	override fun noPath(): Boolean{
		return super.noPath() && beelineSpeed == 0.0
	}
	
	override fun tryMoveToXYZ(x: Double, y: Double, z: Double, speed: Double): Boolean{
		return tryBeelineTo(x, y, z, speed) || super.tryMoveToXYZ(x, y, z, speed)
	}
	
	override fun tryMoveToEntityLiving(target: Entity, speed: Double): Boolean{
		return tryBeelineTo(target.posX, target.posY, target.posZ, speed) || super.tryMoveToEntityLiving(target, speed)
	}
	
	private fun tryBeelineTo(x: Double, y: Double, z: Double, speed: Double): Boolean{
		if (resetTicks > 0){
			return false
		}
		
		beelineTarget = Vec(x, y, z)
		beelineSpeed = speed
		return true
	}
	
	override fun tick(){
		if (!super.noPath()){
			beelineSpeed = 0.0
			super.tick()
			
			if (resetTicks > 0 && --resetTicks == 0){
				resetStuck()
				
				if (beelineTarget != Vec3.ZERO){
					beelineSpeed = speed
					timeoutTimer = 0L
					super.clearPath()
				}
			}
		}
		else if (isBeelining){
			doBeelineMovement()
		}
	}
	
	private fun doBeelineMovement(){
		val targetDistSq = entity.posVec.squareDistanceTo(beelineTarget)
		val movedDistSq = square(entity.posX - lastX) + square(entity.posZ - lastZ)
		
		if (targetDistSq > square(2.5) && movedDistSq < square(0.33)){
			if (++stuckTicks > maxStuckTicks){
				onStuck()
				return
			}
		}
		else if (movedDistSq < square(1.33) && ++stuckLongTicks > maxStuckTicks * 3){
			onStuck()
			return
		}
		else{
			resetStuck()
		}
		
		if (targetDistSq >= square(entity.width)){
			entity.moveHelper.setMoveTo(beelineTarget.x, beelineTarget.y, beelineTarget.z, beelineSpeed)
		}
		else{
			clearPath()
		}
	}
	
	private fun onStuck(){
		stuckTicks = 0
		stuckLongTicks = 0
		
		resetTicks = entity.rng.nextInt(fallbackPathfindingResetTicks)
		timeoutTimer = 0L
		
		if (!super.tryMoveToXYZ(beelineTarget.x, beelineTarget.y, beelineTarget.z, beelineSpeed)){
			beelineSpeed = 0.0
		}
	}
	
	private fun resetStuck(){
		lastX = entity.posX
		lastZ = entity.posZ
		stuckTicks = 0
	}
	
	override fun clearPath(){
		super.clearPath()
		resetStuck()
		stuckLongTicks = 0
		resetTicks = 0
		beelineTarget = Vec3.ZERO
		beelineSpeed = 0.0
	}
}
