package chylex.hee.game.block.util

import chylex.hee.util.forge.supply
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.DirectionalBlock
import net.minecraft.block.HorizontalBlock
import net.minecraft.block.SoundType
import net.minecraft.state.Property
import net.minecraft.util.Direction
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraftforge.common.util.ForgeSoundType

// Properties

fun <T : Comparable<T>, V : T> Block.with(property: Property<T>, value: V): BlockState {
	return this.defaultState.with(property, value)
}

fun BlockState.withFacing(facing: Direction): BlockState {
	if (this.properties.contains(DirectionalBlock.FACING)) {
		return this.with(DirectionalBlock.FACING, facing)
	}
	else if (this.properties.contains(HorizontalBlock.HORIZONTAL_FACING)) {
		return this.with(HorizontalBlock.HORIZONTAL_FACING, facing)
	}
	
	throw UnsupportedOperationException("could not find a facing property on the block")
}

fun Block.withFacing(facing: Direction): BlockState {
	return this.defaultState.withFacing(facing)
}

// Shapes

val AxisAlignedBB.asVoxelShape: VoxelShape
	get() = VoxelShapes.create(this)

// Sound types

fun SoundType.clone(
	volume: Float = this.volume,
	pitch: Float = this.pitch,
	breakSound: SoundEvent = this.breakSound,
	stepSound: SoundEvent = this.stepSound,
	placeSound: SoundEvent = this.placeSound,
	hitSound: SoundEvent = this.hitSound,
	fallSound: SoundEvent = this.fallSound,
) = ForgeSoundType(volume, pitch, supply(breakSound), supply(stepSound), supply(placeSound), supply(hitSound), supply(fallSound))
