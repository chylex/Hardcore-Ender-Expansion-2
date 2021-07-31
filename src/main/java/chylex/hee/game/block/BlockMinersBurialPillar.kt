package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder

class BlockMinersBurialPillar(builder: BlockBuilder) : BlockPillarCustom(builder) {
	override val localization
		get() = LocalizationStrategy.Parenthesized(LocalizationStrategy.ReplaceWords("Miners", "Miner's"), wordCount = 1, wordOffset = 3, fromStart = true)
}
