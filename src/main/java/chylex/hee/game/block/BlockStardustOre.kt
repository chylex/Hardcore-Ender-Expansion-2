package chylex.hee.game.block

import chylex.hee.game.Resource
import chylex.hee.game.Resource.location
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockExperienceComponent
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockHarvestTool
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.block.properties.IBlockStateModelSupplier
import chylex.hee.game.item.util.Tool.Level.STONE
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.random.nextBiasedFloat
import net.minecraft.block.Blocks

object BlockStardustOre : HeeBlockBuilder() {
	init {
		includeFrom(BlockEndOre)
		
		model = IBlockStateModelSupplier {
			BlockStateModel(
				BlockStatePreset.None,
				BlockModel.WithTextures(BlockModel.FromParent(Resource.Custom("block/cube_overlay")), mapOf(
					"particle" to it.location("_particle"),
					"base" to Blocks.END_STONE.location,
				))
			)
		}
		
		renderLayer = CUTOUT
		
		drop = BlockDrop.Manual
		
		tool = BlockHarvestTool.required(STONE, PICKAXE)
		hardness = BlockHardness(hardness = 2.8F, resistance = 8.4F)
		
		components.experience = IBlockExperienceComponent { rand -> (rand.nextBiasedFloat(4F) * 6F).ceilToInt() }
	}
}
