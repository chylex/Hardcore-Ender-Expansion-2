package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModBlocks
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockVoidPortalStorageCrafted(builder: BlockBuilder, aabb: AxisAlignedBB) : BlockVoidPortalCrafted(builder, aabb), ITileEntityProvider{
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return ModBlocks.VOID_PORTAL_STORAGE.createNewTileEntity(world, meta)
	}
	
	override fun onBlockAdded(world: World, pos: BlockPos, state: IBlockState){
		BlockAbstractPortal.spawnInnerBlocks(world, pos, ModBlocks.VOID_PORTAL_FRAME_CRAFTED, ModBlocks.VOID_PORTAL_INNER, minSize = 3)
	}
	
	override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		return ModBlocks.VOID_PORTAL_STORAGE.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ)
	}
}
