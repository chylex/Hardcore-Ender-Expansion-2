package chylex.hee.util.math

import chylex.hee.game.world.util.max
import chylex.hee.game.world.util.min
import net.minecraft.util.math.BlockPos

class BoundingBox(pos1: BlockPos, pos2: BlockPos) {
	val min = pos1.min(pos2)
	val max = pos1.max(pos2)
	
	val size
		get() = Size(max.x - min.x + 1, max.y - min.y + 1, max.z - min.z + 1)
	
	val center
		get() = Pos((min.x + max.x) / 2, (min.y + max.y) / 2, (min.z + max.z) / 2)
	
	val centerVec
		get() = Vec((min.x + max.x + 1) * 0.5, (min.y + max.y + 1) * 0.5, (min.z + max.z + 1) * 0.5)
	
	fun intersects(bb: BoundingBox): Boolean {
		return !(
			bb.max.x < min.x || bb.max.y < min.y || bb.max.z < min.z ||
			bb.min.x > max.x || bb.min.y > max.y || bb.min.z > max.z
		)
	}
	
	fun isInside(bb: BoundingBox): Boolean {
		return (
			min.x >= bb.min.x && min.y >= bb.min.y && min.z >= bb.min.z &&
			max.x <= bb.max.x && max.y <= bb.max.y && max.z <= bb.max.z
		)
	}
	
	fun isInside(pos: BlockPos): Boolean {
		return (
			pos.x.let { it >= min.x && it <= max.x } &&
			pos.y.let { it >= min.y && it <= max.y } &&
			pos.z.let { it >= min.z && it <= max.z }
		)
	}
	
	override fun toString(): String {
		return "BoundingBox (${min.x}, ${min.y}, ${min.z} -> ${max.x}, ${max.y}, ${max.z})"
	}
}
