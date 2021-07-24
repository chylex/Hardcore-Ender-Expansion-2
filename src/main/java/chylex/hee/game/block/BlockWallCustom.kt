package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockStateModels
import net.minecraft.block.Block
import net.minecraft.block.WallBlock
import net.minecraft.tags.BlockTags

open class BlockWallCustom(private val fullBlock: Block) : WallBlock(Properties.from(fullBlock)), IHeeBlock {
	final override val model
		get() = BlockStateModels.Wall(fullBlock)
	
	override val tags
		get() = listOf(BlockTags.WALLS)
}
