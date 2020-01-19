package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.util.getTile
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorld
import net.minecraft.world.World

class BlockEndPortalAcceptor(builder: BlockBuilder, aabb: AxisAlignedBB) : BlockSimpleShaped(builder, aabb){
	override fun hasTileEntity(state: BlockState): Boolean{
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return TileEntityEndPortalAcceptor()
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean){
		BlockAbstractPortal.spawnInnerBlocks(world, pos, ModBlocks.END_PORTAL_FRAME, ModBlocks.END_PORTAL_INNER, minSize = 1)
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState{
		if (!world.isRemote && facing == UP){
			pos.getTile<TileEntityEndPortalAcceptor>(world)?.refreshClusterState()
		}
		
		return super.updatePostPlacement(state, facing, neighborState, world, pos, neighborPos)
	}
}
