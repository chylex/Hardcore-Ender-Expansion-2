package chylex.hee.game.block

import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModBlocks
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Direction
import net.minecraft.util.Direction.UP
import net.minecraft.util.Hand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorld
import net.minecraft.world.World

class BlockEndPortalAcceptor(builder: BlockBuilder, aabb: AxisAlignedBB) : BlockSimpleShaped(builder, aabb) {
	override fun hasTileEntity(state: BlockState): Boolean {
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityEndPortalAcceptor()
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
		BlockAbstractPortal.spawnInnerBlocks(world, pos, ModBlocks.END_PORTAL_FRAME, ModBlocks.END_PORTAL_INNER, minSize = 1)
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType {
		if (player.isCreative && player.isSneaking) {
			pos.getTile<TileEntityEndPortalAcceptor>(world)?.toggleChargeFromCreativeMode()
			return SUCCESS
		}
		
		return PASS
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState {
		if (!world.isRemote && facing == UP) {
			pos.getTile<TileEntityEndPortalAcceptor>(world)?.refreshClusterState()
		}
		
		@Suppress("DEPRECATION")
		return super.updatePostPlacement(state, facing, neighborState, world, pos, neighborPos)
	}
}
