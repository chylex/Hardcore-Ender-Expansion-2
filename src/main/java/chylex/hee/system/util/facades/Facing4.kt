package chylex.hee.system.util.facades
import chylex.hee.system.util.nextItem
import com.google.common.collect.Collections2
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.Vec3d
import java.util.Random

object Facing4 : List<EnumFacing> by EnumFacing.HORIZONTALS.toList(){
	private val allPermutations: Array<List<EnumFacing>> = Collections2.permutations(this).toTypedArray()
	fun randomPermutation(rand: Random) = rand.nextItem(allPermutations)
	
	fun fromDirection(direction: Vec3d): EnumFacing = EnumFacing.getFacingFromVector(direction.x.toFloat(), 0F, direction.z.toFloat())
	fun fromDirection(source: Vec3d, target: Vec3d) = fromDirection(target.subtract(source))
}
