package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockHarvestTool
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.game.block.properties.Materials
import chylex.hee.game.item.util.Tool.Level.DIAMOND
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.MaterialColor
import net.minecraftforge.common.Tags
import net.minecraftforge.common.ToolType.PICKAXE

object BlockObsidianCube : HeeBlockBuilder() {
	private val DEFAULT_LOCALIZATION = LocalizationStrategy.MoveToBeginning(wordCount = 1, wordOffset = 1, fromStart = true)
	
	init {
		localization = DEFAULT_LOCALIZATION
		
		tags.add(Tags.Blocks.OBSIDIAN)
		
		material = Materials.SOLID
		color = MaterialColor.BLACK
		sound = SoundType.STONE
		tool = BlockHarvestTool.required(DIAMOND, PICKAXE)
		hardness = BlockHardness(hardness = 20F, resistance = 300F)
	}
	
	class Lit(modelBlock: Block) : HeeBlockBuilder() {
		init {
			includeFrom(BlockObsidianCube)
			
			localization = LocalizationStrategy.Parenthesized(DEFAULT_LOCALIZATION, wordCount = 1, fromStart = false)
			model = BlockStateModels.Cube(modelBlock)
			
			light = 15
		}
	}
	
	class TowerTop(modelBlock: Block) : HeeBlockBuilder() {
		init {
			includeFrom(BlockIndestructible)
			includeFrom(Lit(modelBlock))
			
			localization = LocalizationStrategy.Default
		}
	}
}
