package chylex.hee.game.block

import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockAddedComponent
import chylex.hee.game.block.components.IBlockEntityComponent
import chylex.hee.game.block.components.IBlockNameComponent
import chylex.hee.game.block.components.IPlayerUseBlockComponent
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModContainers
import net.minecraft.util.ActionResultType.SUCCESS

abstract class BlockVoidPortalStorage(base: HeeBlockBuilder, minPortalSize: Int) : HeeBlockBuilder() {
	init {
		includeFrom(base)
		
		model = BlockModel.PortalFrame(ModBlocks.VOID_PORTAL_FRAME, "storage")
		
		components.entity = IBlockEntityComponent(::TileEntityVoidPortalStorage)
		
		components.onAdded = IBlockAddedComponent { _, world, pos ->
			BlockAbstractPortal.spawnInnerBlocks(world, pos, ModBlocks.VOID_PORTAL_FRAME, ModBlocks.VOID_PORTAL_INNER, minSize = minPortalSize)
		}
		
		components.playerUse = IPlayerUseBlockComponent { _, world, pos, player, _ ->
			pos.getTile<TileEntityVoidPortalStorage>(world)?.let { ModContainers.open(player, it, pos) }
			SUCCESS
		}
	}
	
	object Indestructible : BlockVoidPortalStorage(BlockVoidPortalFrame.Indestructible, minPortalSize = 1)
	
	object Crafted : BlockVoidPortalStorage(BlockVoidPortalFrame.Crafted, minPortalSize = 3) {
		init {
			components.name = IBlockNameComponent.of(ModBlocks.VOID_PORTAL_STORAGE)
		}
	}
}
