package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModGuiHandler.GuiType
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.getTile
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Hand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class BlockVoidPortalStorage(builder: BlockBuilder, aabb: AxisAlignedBB) : BlockSimpleShaped(builder, aabb){
	override fun hasTileEntity(state: BlockState): Boolean{
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return TileEntityVoidPortalStorage()
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean){
		BlockAbstractPortal.spawnInnerBlocks(world, pos, ModBlocks.VOID_PORTAL_FRAME, ModBlocks.VOID_PORTAL_INNER, minSize = 1)
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, hand: Hand, hit: BlockRayTraceResult): Boolean{
		if (world.isRemote){
			return true
		}
		
		pos.getTile<TileEntityVoidPortalStorage>(world)?.let {
			GuiType.PORTAL_TOKEN_STORAGE.open(player, pos.x, pos.y, pos.z)
		}
		
		return true
	}
}
