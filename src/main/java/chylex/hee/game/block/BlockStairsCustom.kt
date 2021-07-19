package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.util.forge.supply
import net.minecraft.block.Block
import net.minecraft.block.StairsBlock

open class BlockStairsCustom(private val fullBlock: Block) : StairsBlock(supply(fullBlock.defaultState), Properties.from(fullBlock)), IHeeBlock {
	override val model
		get() = BlockStateModels.Stairs(fullBlock)
}
