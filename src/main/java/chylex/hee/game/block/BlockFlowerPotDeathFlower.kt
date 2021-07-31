package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder
import net.minecraft.block.Block

class BlockFlowerPotDeathFlower(builder: BlockBuilder, flower: Block) : BlockFlowerPotCustom(builder, flower) {
	override val localization
		get() = LocalizationStrategy.MoveToBeginning(super.localization, wordCount = 1, wordOffset = 1, fromStart = true)
}
