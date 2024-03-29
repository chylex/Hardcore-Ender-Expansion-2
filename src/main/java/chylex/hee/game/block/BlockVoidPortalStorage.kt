package chylex.hee.game.block

import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModContainers
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class BlockVoidPortalStorage(builder: BlockBuilder) : BlockPortalFrame(builder) {
	override val model
		get() = BlockModel.PortalFrame(ModBlocks.VOID_PORTAL_FRAME, "storage")
	
	override fun hasTileEntity(state: BlockState): Boolean {
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityVoidPortalStorage()
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
		BlockAbstractPortal.spawnInnerBlocks(world, pos, ModBlocks.VOID_PORTAL_FRAME, ModBlocks.VOID_PORTAL_INNER, minSize = 1)
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType {
		if (world.isRemote) {
			return SUCCESS
		}
		
		pos.getTile<TileEntityVoidPortalStorage>(world)?.let {
			ModContainers.open(player, it, pos)
		}
		
		return SUCCESS
	}
}
