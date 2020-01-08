@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.system.util
import chylex.hee.system.migration.vanilla.BlockDirectional
import chylex.hee.system.migration.vanilla.BlockHorizontal
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.state.IProperty
import net.minecraft.util.Direction
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes

// Properties

inline fun <T : Comparable<T>, V : T> Block.with(property: IProperty<T>, value: V): BlockState{
	return this.defaultState.with(property, value)
}

// Shapes

val AxisAlignedBB.asVoxelShape: VoxelShape
	get() = VoxelShapes.create(this)

// Facing

fun BlockState.withFacing(facing: Direction): BlockState{
	if (this.properties.contains(BlockDirectional.FACING)){
		return this.with(BlockDirectional.FACING, facing)
	}
	else if (this.properties.contains(BlockHorizontal.HORIZONTAL_FACING)){
		return this.with(BlockHorizontal.HORIZONTAL_FACING, facing)
	}
	
	throw UnsupportedOperationException("could not find a facing property on the block")
}

fun Block.withFacing(facing: Direction): BlockState{
	return this.defaultState.withFacing(facing)
}
