package chylex.hee.game.entity.living.path
import chylex.hee.game.entity.living.controller.EntityMoveFlyingForward
import chylex.hee.game.entity.lookDirVec
import chylex.hee.game.entity.lookPosVec
import chylex.hee.game.entity.posVec
import chylex.hee.system.math.directionTowards
import chylex.hee.system.math.sign
import chylex.hee.system.math.square
import chylex.hee.system.math.withY
import chylex.hee.system.migration.EntityLiving
import net.minecraft.entity.Entity
import net.minecraft.pathfinding.FlyingPathNavigator
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class PathNavigateFlyingPreferBeeLineOrStrafe(entity: EntityLiving, world: World) : FlyingPathNavigator(entity, world){
	private var moveTarget = Vec3d.ZERO
	private var moveSpeed = 0.0
	
	private val isBeelining
		get() = moveSpeed > 0.0
	
	private var strafeDir = 0
	private var strafeDirReset = 0
	
	override fun noPath(): Boolean{
		return super.noPath() && moveSpeed == 0.0
	}
	
	override fun tryMoveToXYZ(x: Double, y: Double, z: Double, speed: Double): Boolean{
		return tryBeelineTo(x, y, z, speed) || super.tryMoveToXYZ(x, y, z, speed)
	}
	
	override fun tryMoveToEntityLiving(target: Entity, speed: Double): Boolean{
		return tryBeelineTo(target.posX, target.posY, target.posZ, speed) || super.tryMoveToEntityLiving(target, speed)
	}
	
	private fun tryBeelineTo(x: Double, y: Double, z: Double, speed: Double): Boolean{
		moveTarget = Vec3d(x, y, z)
		moveSpeed = speed
		return true
	}
	
	override fun tick(){
		if (!isBeelining){
			return
		}
		
		val moveHelper = entity.moveHelper
		
		if (entity.collided && entity.motion.lengthSquared() < square(moveSpeed * 0.1)){
			strafeDirReset = 35
			
			if (strafeDir == 0){
				strafeDir = 1
				
				val path = getPathToPos(moveTarget.x, moveTarget.y, moveTarget.z, 1)
				
				if (path != null && !path.isFinished){
					val point = path.getPathPointFromIndex(0)
					val pointDir = entity.lookPosVec.withY(0.0).directionTowards(Vec3d(point.x + 0.5, 0.0, point.z + 0.5))
					
					strafeDir = entity.lookDirVec.sign(pointDir)
				}
			}
			
			val strafe = moveTarget.subtract(entity.posVec).withY(0.0).normalize().crossProduct(Vec3d(0.0, strafeDir.toDouble(), 0.0))
			val target = entity.posVec.withY(moveTarget.y).add(strafe)
			
			moveHelper.setMoveTo(target.x, target.y, target.z, moveSpeed)
			(moveHelper as? EntityMoveFlyingForward)?.strafe()
			
			// TODO improve vertical movement for short obstacles and eventually change the strategy completely for more open spaces
		}
		else{
			moveHelper.setMoveTo(moveTarget.x, moveTarget.y, moveTarget.z, moveSpeed)
			
			if (strafeDirReset > 0 && --strafeDirReset == 0){
				strafeDir = 0
			}
		}
	}
	
	override fun clearPath(){
		super.clearPath()
		moveTarget = Vec3d.ZERO
		moveSpeed = 0.0
	}
}
