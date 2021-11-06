package chylex.hee.game.block

import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockNameComponent
import chylex.hee.game.block.logic.IBlockDynamicHardness
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockHarvestTool
import chylex.hee.game.item.util.Tool.Level.DIAMOND
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.getBlock
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModTags
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraftforge.common.ToolType.PICKAXE

object BlockVoidPortalFrame {
	object Indestructible : HeeBlockBuilder() {
		init {
			includeFrom(BlockPortalFrameIndestructible)
		}
	}
	
	object Crafted : HeeBlockBuilder() {
		init {
			includeFrom(BlockPortalFrameBase)
			
			model = ModBlocks.VOID_PORTAL_FRAME.model.generate(ModBlocks.VOID_PORTAL_FRAME)
			
			tags.add(ModTags.VOID_PORTAL_FRAME_CRAFTED)
			
			tool = BlockHarvestTool.required(DIAMOND, PICKAXE)
			hardness = BlockHardness(hardnessAndResistance = 1.7F)
			
			components.name = IBlockNameComponent.of(ModBlocks.VOID_PORTAL_FRAME)
			
			interfaces[IBlockDynamicHardness::class.java] = object : IBlockDynamicHardness {
				override fun getBlockHardness(world: IBlockReader, pos: BlockPos, state: BlockState, originalHardness: Float): Float {
					return if (Facing4.any { pos.offset(it).getBlock(world) === ModBlocks.VOID_PORTAL_INNER })
						originalHardness * 20F
					else
						originalHardness
				}
			}
		}
	}
}
