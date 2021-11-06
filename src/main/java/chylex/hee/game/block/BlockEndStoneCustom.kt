package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.Resource.location
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockHarvestTool
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.IBlockStateModelSupplier
import chylex.hee.game.item.util.Tool.Level.WOOD
import net.minecraft.block.Blocks
import net.minecraft.block.material.MaterialColor
import net.minecraftforge.common.Tags
import net.minecraftforge.common.ToolType.PICKAXE

object BlockEndStoneCustom : HeeBlockBuilder() {
	init {
		includeFrom(BlockEndStoneBase)
		
		localization = LocalizationStrategy.MoveToBeginning(wordCount = 1)
		
		model = IBlockStateModelSupplier {
			BlockModel.WithTextures(
				BlockModel.CubeBottomTop(bottom = Blocks.END_STONE.location),
				mapOf("particle" to it.location("_top"))
			)
		}
		
		tags.add(Tags.Blocks.END_STONES)
		
		tool = BlockHarvestTool.required(WOOD, PICKAXE)
		hardness = BlockHardness(hardness = 3F, resistance = 9F)
	}
	
	val INFESTED = HeeBlockBuilder {
		includeFrom(BlockEndStoneCustom)
		color = MaterialColor.RED
	}
	
	val BURNED = HeeBlockBuilder {
		includeFrom(BlockEndStoneCustom)
		color = MaterialColor.ADOBE // RENAME ORANGE
	}
	
	val ENCHANTED = HeeBlockBuilder {
		includeFrom(BlockEndStoneCustom)
		color = MaterialColor.PURPLE
	}
}
