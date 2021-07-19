package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockStateModels
import net.minecraft.block.Block

class BlockObsidianCubeLit(builder: BlockBuilder, private val modelBlock: Block) : HeeBlock(builder) {
	override val model
		get() = BlockStateModels.Cube(modelBlock)
}
