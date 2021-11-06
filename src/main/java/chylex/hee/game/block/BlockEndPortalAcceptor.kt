package chylex.hee.game.block

import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockAddedComponent
import chylex.hee.game.block.components.IBlockEntityComponent
import chylex.hee.game.block.components.IBlockNeighborUpdatedComponent
import chylex.hee.game.block.components.IPlayerUseBlockComponent
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModBlocks
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Direction.UP
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object BlockEndPortalAcceptor : HeeBlockBuilder() {
	init {
		includeFrom(BlockPortalFrameIndestructible)
		
		model = BlockModel.PortalFrame(ModBlocks.END_PORTAL_FRAME, "acceptor")
		
		components.entity = IBlockEntityComponent(::TileEntityEndPortalAcceptor)
		
		components.onAdded = IBlockAddedComponent { _, world, pos ->
			BlockAbstractPortal.spawnInnerBlocks(world, pos, ModBlocks.END_PORTAL_FRAME, ModBlocks.END_PORTAL_INNER, minSize = 1)
		}
		
		components.onNeighborUpdated = IBlockNeighborUpdatedComponent { state, pos, world, neighborFacing, _ ->
			if (!world.isRemote && neighborFacing == UP) {
				pos.getTile<TileEntityEndPortalAcceptor>(world)?.refreshClusterState()
			}
			
			state
		}
		
		components.playerUse = object : IPlayerUseBlockComponent {
			override fun use(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand): ActionResultType {
				if (player.isCreative && player.isSneaking) {
					pos.getTile<TileEntityEndPortalAcceptor>(world)?.toggleChargeFromCreativeMode()
					return SUCCESS
				}
				
				return PASS
			}
		}
	}
}
