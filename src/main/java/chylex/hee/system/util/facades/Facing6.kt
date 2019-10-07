package chylex.hee.system.util.facades
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.Vec3d

object Facing6 : List<EnumFacing> by EnumFacing.VALUES.toList(){
	fun fromDirection(source: Vec3d, target: Vec3d): EnumFacing = EnumFacing.getFacingFromVector((target.x - source.x).toFloat(), (target.y - source.y).toFloat(), (target.z - source.z).toFloat())
}
