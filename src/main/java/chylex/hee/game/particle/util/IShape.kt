package chylex.hee.game.particle.util
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.center
import chylex.hee.system.util.offsetTowards
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.Collections

interface IShape{
	val points: Iterable<Vec3d>
	
	// General
	
	class Point(point: Vec3d, amount: Int) : IShape{
		constructor(pos: BlockPos, amount: Int) : this(pos.center, amount)
		constructor(entity: Entity, heightMp: Float, amount: Int) : this(Vec3d(entity.posX, entity.posY + entity.height * heightMp, entity.posZ), amount)
		constructor(x: Double, y: Double, z: Double, amount: Int) : this(Vec3d(x, y, z), amount)
		
		override val points: Collection<Vec3d> = Collections.nCopies(amount, point)
	}
	
	class Line(startPoint: Vec3d, endPoint: Vec3d, points: Int) : IShape{
		constructor(startPos: BlockPos, endPos: BlockPos, points: Int) : this(startPos.center, endPos.center, points)
		
		constructor(startPoint: Vec3d, endPoint: Vec3d, distanceBetweenPoints: Double) : this(startPoint, endPoint, (startPoint.distanceTo(endPoint) / distanceBetweenPoints).ceilToInt())
		constructor(startPos: BlockPos, endPos: BlockPos, distanceBetweenPoints: Double) : this(startPos.center, endPos.center, distanceBetweenPoints)
		
		override val points: Collection<Vec3d> = if (points == 1)
			listOf(startPoint)
		else
			(0 until points).map { startPoint.offsetTowards(endPoint, it.toDouble() / (points - 1)) }
	}
}
