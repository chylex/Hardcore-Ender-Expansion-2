package chylex.hee.game.world.util
import chylex.hee.system.util.Pos
import chylex.hee.system.util.max
import chylex.hee.system.util.min
import net.minecraft.util.math.BlockPos

class BoundingBox(pos1: BlockPos, pos2: BlockPos){
	val min = pos1.min(pos2)
	val max = pos1.max(pos2)
	
	val center: BlockPos
		get() = Pos((min.x + max.x) / 2, (min.y + max.y) / 2, (min.z + max.z) / 2)
	
	fun intersects(bb: BoundingBox): Boolean{
		return !(
			bb.max.x < min.x || bb.max.y < min.y || bb.max.z < min.z ||
			bb.min.x > max.x || bb.min.y > max.y || bb.min.z > max.z
		)
	}
	
	fun isInside(bb: BoundingBox): Boolean{
		return (
			min.x >= bb.min.x && min.y >= bb.min.y && min.z >= bb.min.z &&
			max.x <= bb.max.x && max.y <= bb.max.y && max.z <= bb.max.z
		)
	}
	
	override fun toString(): String{
		return "BoundingBox (${min.x}, ${min.y}, ${min.z} -> ${max.x}, ${max.y}, ${max.z})"
	}
}
