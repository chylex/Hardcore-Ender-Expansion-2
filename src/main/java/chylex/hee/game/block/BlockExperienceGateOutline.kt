package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.with
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockExperienceGateOutline(builder: BlockBuilder) : BlockExperienceGate(builder){
	companion object{
		val NEIGHBOR_NORTH = Property.bool("neighbor_north")
		val NEIGHBOR_WEST = Property.bool("neighbor_west")
		val IS_STRAIGHT = Property.bool("is_straight")
	}
	
	init{
		defaultState = blockState.baseState.with(NEIGHBOR_NORTH, false).with(NEIGHBOR_WEST, false).with(IS_STRAIGHT, false)
	}
	
	override fun createBlockState() = BlockStateContainer(this, NEIGHBOR_NORTH, NEIGHBOR_WEST, IS_STRAIGHT)
	
	override fun getMetaFromState(state: IBlockState) = 0
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState
	
	private fun checkSide(world: IBlockAccess, pos: BlockPos, facing: EnumFacing): Boolean{
		return pos.offset(facing).getBlock(world) === this
	}
	
	override fun getActualState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState{
		val north = checkSide(world, pos, NORTH)
		val west = checkSide(world, pos, WEST)
		
		return state
			.with(NEIGHBOR_NORTH, north)
			.with(NEIGHBOR_WEST, west)
			.with(IS_STRAIGHT, (north && checkSide(world, pos, SOUTH)) || (west && checkSide(world, pos, EAST)))
	}
}
