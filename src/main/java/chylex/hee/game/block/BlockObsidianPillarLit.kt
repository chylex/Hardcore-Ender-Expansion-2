package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.init.ModBlocks

class BlockObsidianPillarLit(builder: BlockBuilder) : BlockPillarCustom(builder) {
	override val model
		get() = BlockStateModels.Pillar(ModBlocks.OBSIDIAN_PILLAR)
}
