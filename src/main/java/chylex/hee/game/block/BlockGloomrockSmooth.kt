package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder

class BlockGloomrockSmooth(builder: BlockBuilder) : BlockGloomrock(builder) {
	override val localization
		get() = LocalizationStrategy.MoveToBeginning(wordCount = 1)
}
