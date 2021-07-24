package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.util.forge.supply
import net.minecraft.block.Block
import net.minecraft.block.StairsBlock
import net.minecraft.tags.BlockTags

open class BlockStairsCustom(protected val fullBlock: Block) : StairsBlock(supply(fullBlock.defaultState), Properties.from(fullBlock)), IHeeBlock {
	override val model
		get() = BlockStateModels.Stairs(fullBlock)
	
	override val tags
		get() = listOf(BlockTags.STAIRS)
}
