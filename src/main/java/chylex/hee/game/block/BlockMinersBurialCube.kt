package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder

class BlockMinersBurialCube(builder: BlockBuilder) : HeeBlock(builder) {
	override val localization
		get() = LocalizationStrategy.Parenthesized(LocalizationStrategy.DeleteWords(LocalizationStrategy.ReplaceWords("Miners", "Miner's"), "Plain"), wordCount = 1, wordOffset = 3, fromStart = true)
}
