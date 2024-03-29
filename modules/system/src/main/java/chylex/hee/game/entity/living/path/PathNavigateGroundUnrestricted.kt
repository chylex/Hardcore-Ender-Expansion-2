package chylex.hee.game.entity.living.path

import chylex.hee.game.entity.util.posVec
import chylex.hee.util.math.Vec
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.square
import net.minecraft.entity.Entity
import net.minecraft.entity.MobEntity
import net.minecraft.pathfinding.GroundPathNavigator
import net.minecraft.world.World

class PathNavigateGroundUnrestricted(entity: MobEntity, world: World) : GroundPathNavigator(entity, world) {
	private var overrideTarget = Vec3.ZERO
	private var overrideSpeed = 0.0
	
	override fun noPath(): Boolean {
		return super.noPath() && overrideSpeed == 0.0
	}
	
	override fun tryMoveToXYZ(x: Double, y: Double, z: Double, speed: Double): Boolean {
		val path = pathfind(x, y, z, 1)
		
		if (path != null) {
			overrideSpeed = 0.0
			return setPath(path, speed)
		}
		
		overrideTarget = Vec(x, y, z)
		overrideSpeed = speed
		timeoutTimer = 0L
		return true
	}
	
	override fun tryMoveToEntityLiving(entity: Entity, speed: Double): Boolean {
		val path = pathfind(entity, 1)
		
		if (path != null) {
			overrideSpeed = 0.0
			return setPath(path, speed)
		}
		
		overrideTarget = entity.posVec
		overrideSpeed = speed
		timeoutTimer = 0L
		return true
	}
	
	override fun tick() {
		if (!super.noPath()) {
			overrideSpeed = 0.0
			super.tick()
		}
		else if (overrideSpeed > 0.0) {
			val entityPos = entity.posVec
			val minDistSq = square(entity.width)
			
			if (entityPos.squareDistanceTo(overrideTarget) >= minDistSq && (entity.posY <= overrideTarget.y || entityPos.squareDistanceTo(overrideTarget.x, entityPos.y.floorToInt().toDouble(), overrideTarget.z) >= minDistSq)) {
				entity.moveHelper.setMoveTo(overrideTarget.x, overrideTarget.y, overrideTarget.z, overrideSpeed)
			}
			else {
				overrideSpeed = 0.0
			}
		}
	}
	
	override fun clearPath() {
		super.clearPath()
		overrideSpeed = 0.0
	}
}
