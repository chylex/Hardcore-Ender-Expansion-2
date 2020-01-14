package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.getBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorld

class BlockExperienceGateOutline(builder: BlockBuilder) : BlockExperienceGate(builder){
	companion object{
		val NEIGHBOR_NORTH = Property.bool("neighbor_north")
		val NEIGHBOR_WEST = Property.bool("neighbor_west")
		val IS_STRAIGHT = Property.bool("is_straight")
	}
	
	init{
		defaultState = stateContainer.baseState.with(NEIGHBOR_NORTH, false).with(NEIGHBOR_WEST, false).with(IS_STRAIGHT, false)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(NEIGHBOR_NORTH, NEIGHBOR_WEST, IS_STRAIGHT)
	}
	
	private fun checkSide(world: IBlockReader, pos: BlockPos, facing: Direction): Boolean{
		return pos.offset(facing).getBlock(world) === this
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState{
		val north = checkSide(world, pos, NORTH)
		val west = checkSide(world, pos, WEST)
		
		return state
			.with(NEIGHBOR_NORTH, north)
			.with(NEIGHBOR_WEST, west)
			.with(IS_STRAIGHT, (north && checkSide(world, pos, SOUTH)) || (west && checkSide(world, pos, EAST)))
	}
}
