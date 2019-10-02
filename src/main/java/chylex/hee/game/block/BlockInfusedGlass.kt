package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Facing6
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.with
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockRenderLayer.TRANSLUCENT
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class BlockInfusedGlass(builder: BlockBuilder) : BlockSimple(builder){
	companion object{
		val CONNECT_DOWN = Property.bool("connect_d")
		val CONNECT_UP = Property.bool("connect_u")
		val CONNECT_NORTH = Property.bool("connect_n")
		val CONNECT_SOUTH = Property.bool("connect_s")
		val CONNECT_EAST = Property.bool("connect_e")
		val CONNECT_WEST = Property.bool("connect_w")
		
		private val CONNECT_MAPPINGS = mapOf(
			DOWN to CONNECT_DOWN,
			UP to CONNECT_UP,
			NORTH to CONNECT_NORTH,
			SOUTH to CONNECT_SOUTH,
			EAST to CONNECT_EAST,
			WEST to CONNECT_WEST
		)
	}
	
	init{
		defaultState = Facing6.fold(blockState.baseState){ acc, facing -> acc.with(CONNECT_MAPPINGS.getValue(facing), false) }
	}
	
	override fun createBlockState() = BlockStateContainer(this, CONNECT_DOWN, CONNECT_UP, CONNECT_NORTH, CONNECT_SOUTH, CONNECT_EAST, CONNECT_WEST)
	
	override fun getMetaFromState(state: IBlockState) = 0
	
	override fun getActualState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState{
		return Facing6.fold(state){ acc, facing -> acc.with(CONNECT_MAPPINGS.getValue(facing), pos.offset(facing).getBlock(world) === this) } // TODO improve corners
	}
	
	override fun quantityDropped(rand: Random) = 0
	override fun canSilkHarvest() = true
	
	override fun canPlaceTorchOnTop(state: IBlockState, world: IBlockAccess, pos: BlockPos) = true
	
	override fun isFullCube(state: IBlockState) = false
	override fun isOpaqueCube(state: IBlockState) = false
	override fun getRenderLayer() = TRANSLUCENT
	
	@SideOnly(Side.CLIENT)
	override fun shouldSideBeRendered(state: IBlockState, world: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean{
		return pos.offset(side).getBlock(world) !== this
	}
}
