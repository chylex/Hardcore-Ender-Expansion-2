package chylex.hee.game.entity.living.ai.path
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.posVec
import chylex.hee.system.util.square
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.pathfinding.PathNavigateGround
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class PathNavigateGroundUnrestricted(entity: EntityLiving, world: World) : PathNavigateGround(entity, world){
	private var overrideTarget = Vec3d.ZERO
	private var overrideSpeed = 0.0
	
	override fun noPath(): Boolean{
		return super.noPath() || overrideSpeed > 0.0
	}
	
	override fun tryMoveToXYZ(x: Double, y: Double, z: Double, speed: Double): Boolean{
		val path = getPathToXYZ(x, y, z)
		
		if (path != null){
			overrideSpeed = 0.0
			return setPath(path, speed)
		}
		
		overrideTarget = Vec3d(x, y, z)
		overrideSpeed = speed
		return true
	}
	
	override fun tryMoveToEntityLiving(entity: Entity, speed: Double): Boolean{
		val path = getPathToEntityLiving(entity)
		
		if (path != null){
			overrideSpeed = 0.0
			return setPath(path, speed)
		}
		
		overrideTarget = entity.posVec
		overrideSpeed = speed
		return true
	}
	
	override fun onUpdateNavigation(){
		if (!super.noPath()){
			super.onUpdateNavigation()
		}
		else if (overrideSpeed > 0.0){
			val entityPos = entity.posVec
			val minDistSq = square(entity.width)
			
			if (entityPos.squareDistanceTo(overrideTarget) >= minDistSq && (entity.posY <= overrideTarget.y || entityPos.squareDistanceTo(overrideTarget.x, entityPos.y.floorToInt().toDouble(), overrideTarget.z) >= minDistSq)){
				entity.moveHelper.setMoveTo(overrideTarget.x, overrideTarget.y, overrideTarget.z, overrideSpeed)
			}
			else{
				overrideSpeed = 0.0
			}
		}
	}
}
