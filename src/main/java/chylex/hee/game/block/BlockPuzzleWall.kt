package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder

class BlockPuzzleWall(builder: BlockBuilder) : HeeBlock(builder) {
	override val localization
		get() = LocalizationStrategy.Parenthesized(wordCount = 1, fromStart = false)
}
