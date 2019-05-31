package chylex.hee.system.util
import com.google.common.collect.Collections2
import net.minecraft.block.Block
import net.minecraft.block.BlockDirectional
import net.minecraft.block.BlockHorizontal
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.Rotation
import net.minecraft.util.math.Vec3d
import java.util.Random

// Facing

object Facing4 : List<EnumFacing> by EnumFacing.HORIZONTALS.toList(){
	private val allPermutations: Array<List<EnumFacing>> = Collections2.permutations(this).toTypedArray()
	fun randomPermutation(rand: Random) = rand.nextItem(allPermutations)
	
	fun fromDirection(source: Vec3d, target: Vec3d): EnumFacing = EnumFacing.getFacingFromVector((target.x - source.x).toFloat(), 0F, (target.z - source.z).toFloat())
}

object Facing6 : List<EnumFacing> by EnumFacing.VALUES.toList(){
	fun fromDirection(source: Vec3d, target: Vec3d): EnumFacing = EnumFacing.getFacingFromVector((target.x - source.x).toFloat(), (target.y - source.y).toFloat(), (target.z - source.z).toFloat())
}

// Rotation

object Rotation4 : List<Rotation> by Rotation.values().toList(){
	private val allPermutations: Array<List<Rotation>> = Collections2.permutations(this).toTypedArray()
	fun randomPermutation(rand: Random) = rand.nextItem(allPermutations)
}

// Extensions

fun IBlockState.withFacing(facing: EnumFacing): IBlockState{
	if (this.properties.containsKey(BlockDirectional.FACING)){
		return this.withProperty(BlockDirectional.FACING, facing)
	}
	else if (this.properties.containsKey(BlockHorizontal.FACING)){
		return this.withProperty(BlockHorizontal.FACING, facing)
	}
	
	throw UnsupportedOperationException("could not find a facing property on the block")
}

fun Block.withFacing(facing: EnumFacing): IBlockState{
	return this.defaultState.withFacing(facing)
}
