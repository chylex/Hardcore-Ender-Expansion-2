package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockStateModels
import net.minecraft.block.Block
import net.minecraft.block.SlabBlock
import net.minecraft.tags.BlockTags

open class BlockSlabCustom(protected val fullBlock: Block) : SlabBlock(Properties.from(fullBlock)), IHeeBlock {
	override val model
		get() = BlockStateModels.Slab(fullBlock)
	
	override val tags
		get() = listOf(BlockTags.SLABS)
}
