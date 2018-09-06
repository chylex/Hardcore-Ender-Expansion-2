package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.system.util.getTile
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockEndPortalAcceptor(builder: BlockSimple.Builder, aabb: AxisAlignedBB) : BlockSimpleShaped(builder, aabb), ITileEntityProvider{
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityEndPortalAcceptor()
	}
	
	override fun neighborChanged(state: IBlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos){
		if (neighborPos == pos.up()){
			pos.getTile<TileEntityEndPortalAcceptor>(world)?.refreshClusterState()
		}
	}
}
