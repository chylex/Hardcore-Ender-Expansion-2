package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockStateModels
import net.minecraft.block.Block
import net.minecraft.block.WallBlock

open class BlockWallCustom(private val fullBlock: Block) : WallBlock(Properties.from(fullBlock)), IHeeBlock {
	final override val model
		get() = BlockStateModels.Wall(fullBlock)
}
