package chylex.hee.system.util.facades
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.Vec3d

object Facing6 : List<EnumFacing> by EnumFacing.VALUES.toList(){
	fun fromDirection(direction: Vec3d): EnumFacing = EnumFacing.getFacingFromVector(direction.x.toFloat(), direction.y.toFloat(), direction.z.toFloat())
	fun fromDirection(source: Vec3d, target: Vec3d) = fromDirection(target.subtract(source))
}
