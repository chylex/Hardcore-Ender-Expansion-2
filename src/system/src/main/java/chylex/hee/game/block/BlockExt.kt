@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.game.block

import chylex.hee.system.migration.BlockDirectional
import chylex.hee.system.migration.BlockHorizontal
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.SoundType
import net.minecraft.state.IProperty
import net.minecraft.util.Direction
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes

// Properties

inline fun <T : Comparable<T>, V : T> Block.with(property: IProperty<T>, value: V): BlockState {
	return this.defaultState.with(property, value)
}

fun SoundType.clone(
	volume: Float = this.volume,
	pitch: Float = this.pitch,
	breakSound: SoundEvent = this.breakSound,
	stepSound: SoundEvent = this.stepSound,
	placeSound: SoundEvent = this.placeSound,
	hitSound: SoundEvent = this.hitSound,
	fallSound: SoundEvent = this.fallSound,
) = SoundType(volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound)

// Shapes

val AxisAlignedBB.asVoxelShape: VoxelShape
	get() = VoxelShapes.create(this)

// Facing

fun BlockState.withFacing(facing: Direction): BlockState {
	if (this.properties.contains(BlockDirectional.FACING)) {
		return this.with(BlockDirectional.FACING, facing)
	}
	else if (this.properties.contains(BlockHorizontal.HORIZONTAL_FACING)) {
		return this.with(BlockHorizontal.HORIZONTAL_FACING, facing)
	}
	
	throw UnsupportedOperationException("could not find a facing property on the block")
}

fun Block.withFacing(facing: Direction): BlockState {
	return this.defaultState.withFacing(facing)
}
