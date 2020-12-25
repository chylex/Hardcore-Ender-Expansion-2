package chylex.hee.game.particle.spawner.properties

import chylex.hee.game.world.center
import chylex.hee.system.math.Vec
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.math.offsetTowards
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.Collections
import kotlin.math.nextUp

interface IShape {
	val points: Iterable<Vec3d>
	
	// General
	
	class Point(point: Vec3d, amount: Int) : IShape {
		constructor(pos: BlockPos, amount: Int) : this(pos.center, amount)
		constructor(entity: Entity, heightMp: Float, amount: Int) : this(Vec(entity.posX, entity.posY + entity.height * heightMp, entity.posZ), amount)
		constructor(x: Double, y: Double, z: Double, amount: Int) : this(Vec(x, y, z), amount)
		
		override val points: Collection<Vec3d> = Collections.nCopies(amount, point)
	}
	
	class Line(startPoint: Vec3d, endPoint: Vec3d, points: Int) : IShape {
		constructor(startPos: BlockPos, endPos: BlockPos, points: Int) : this(startPos.center, endPos.center, points)
		
		constructor(startPoint: Vec3d, endPoint: Vec3d, distanceBetweenPoints: Double) : this(startPoint, endPoint, (startPoint.distanceTo(endPoint).let { if (it == 0.0) it.nextUp() else it } / distanceBetweenPoints).ceilToInt())
		constructor(startPos: BlockPos, endPos: BlockPos, distanceBetweenPoints: Double) : this(startPos.center, endPos.center, distanceBetweenPoints)
		
		override val points: Collection<Vec3d> = if (points == 1)
			listOf(startPoint)
		else
			(0 until points).map { startPoint.offsetTowards(endPoint, it.toDouble() / (points - 1)) }
	}
}
