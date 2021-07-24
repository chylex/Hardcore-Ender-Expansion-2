package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockStateModels
import net.minecraft.block.Block
import net.minecraftforge.common.Tags

open class BlockObsidianPillar(builder: BlockBuilder) : BlockPillarCustom(builder) {
	final override val tags
		get() = listOf(Tags.Blocks.OBSIDIAN)
	
	class Lit(builder: BlockBuilder, private val modelBlock: Block) : BlockObsidianPillar(builder) {
		override val model
			get() = BlockStateModels.Pillar(modelBlock)
	}
}
