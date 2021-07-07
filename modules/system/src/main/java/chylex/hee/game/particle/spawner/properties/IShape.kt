package chylex.hee.game.particle.spawner.properties

import chylex.hee.util.math.Vec
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.math.center
import chylex.hee.util.math.lerpTowards
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import java.util.Collections
import kotlin.math.nextUp

interface IShape {
	val points: Iterable<Vector3d>
	
	// General
	
	class Point(point: Vector3d, amount: Int) : IShape {
		constructor(pos: BlockPos, amount: Int) : this(pos.center, amount)
		constructor(entity: Entity, heightMp: Float, amount: Int) : this(Vec(entity.posX, entity.posY + entity.height * heightMp, entity.posZ), amount)
		constructor(x: Double, y: Double, z: Double, amount: Int) : this(Vec(x, y, z), amount)
		
		override val points: Collection<Vector3d> = Collections.nCopies(amount, point)
	}
	
	class Line(startPoint: Vector3d, endPoint: Vector3d, points: Int) : IShape {
		constructor(startPos: BlockPos, endPos: BlockPos, points: Int) : this(startPos.center, endPos.center, points)
		
		constructor(startPoint: Vector3d, endPoint: Vector3d, distanceBetweenPoints: Double) : this(startPoint, endPoint, (startPoint.distanceTo(endPoint).let { if (it == 0.0) it.nextUp() else it } / distanceBetweenPoints).ceilToInt())
		constructor(startPos: BlockPos, endPos: BlockPos, distanceBetweenPoints: Double) : this(startPos.center, endPos.center, distanceBetweenPoints)
		
		override val points: Collection<Vector3d> = if (points == 1)
			listOf(startPoint)
		else
			(0 until points).map { startPoint.lerpTowards(endPoint, it.toDouble() / (points - 1)) }
	}
}
