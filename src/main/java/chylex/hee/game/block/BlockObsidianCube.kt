package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockStateModels
import net.minecraft.block.Block
import net.minecraftforge.common.Tags

open class BlockObsidianCube(builder: BlockBuilder) : HeeBlock(builder) {
	override val localization: LocalizationStrategy
		get() = LocalizationStrategy.MoveToBeginning(wordCount = 1, wordOffset = 1, fromStart = true)
	
	final override val tags
		get() = listOf(Tags.Blocks.OBSIDIAN)
	
	open class Lit(builder: BlockBuilder, private val modelBlock: Block) : BlockObsidianCube(builder) {
		override val localization: LocalizationStrategy
			get() = LocalizationStrategy.Parenthesized(super.localization, wordCount = 1, fromStart = false)
		
		override val model
			get() = BlockStateModels.Cube(modelBlock)
	}
	
	class TowerTop(builder: BlockBuilder, modelBlock: Block) : Lit(builder, modelBlock) {
		override val localization
			get() = LocalizationStrategy.Default
	}
}
