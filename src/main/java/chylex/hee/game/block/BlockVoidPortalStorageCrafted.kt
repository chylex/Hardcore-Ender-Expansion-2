package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.vanilla.EntityPlayer
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class BlockVoidPortalStorageCrafted(builder: BlockBuilder, aabb: AxisAlignedBB) : BlockVoidPortalCrafted(builder, aabb){
	override fun hasTileEntity(state: BlockState): Boolean{
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return ModBlocks.VOID_PORTAL_STORAGE.createTileEntity(state, world)
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean){
		BlockAbstractPortal.spawnInnerBlocks(world, pos, ModBlocks.VOID_PORTAL_FRAME_CRAFTED, ModBlocks.VOID_PORTAL_INNER, minSize = 3)
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, hand: Hand, hit: BlockRayTraceResult): ActionResultType{
		@Suppress("DEPRECATION")
		return ModBlocks.VOID_PORTAL_STORAGE.onBlockActivated(state, world, pos, player, hand, hit)
	}
	
	override fun getTranslationKey(): String{
		return ModBlocks.VOID_PORTAL_STORAGE.translationKey
	}
}
