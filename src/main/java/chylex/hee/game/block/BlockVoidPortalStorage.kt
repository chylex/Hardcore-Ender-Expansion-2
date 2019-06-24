package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModGuiHandler.GuiType
import chylex.hee.system.util.getTile
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockVoidPortalStorage(builder: BlockBuilder, aabb: AxisAlignedBB) : BlockSimpleShaped(builder, aabb), ITileEntityProvider{
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityVoidPortalStorage()
	}
	
	override fun onBlockAdded(world: World, pos: BlockPos, state: IBlockState){
		BlockAbstractPortal.spawnInnerBlocks(world, pos, ModBlocks.VOID_PORTAL_FRAME, ModBlocks.VOID_PORTAL_INNER)
	}
	
	override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		if (world.isRemote){
			return true
		}
		
		pos.getTile<TileEntityVoidPortalStorage>(world)?.let {
			GuiType.PORTAL_TOKEN_STORAGE.open(player, pos.x, pos.y, pos.z)
		}
		
		return true
	}
}
