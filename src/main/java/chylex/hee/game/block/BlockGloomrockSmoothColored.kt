package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder

class BlockGloomrockSmoothColored(builder: BlockBuilder) : BlockGloomrock(builder) {
	override val localization
		get() = LocalizationStrategy.MoveToBeginning(LocalizationStrategy.DeleteWords("Smooth"), wordCount = 1)
}
