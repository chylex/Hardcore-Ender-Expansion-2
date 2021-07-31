package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.Resource
import chylex.hee.game.block.properties.BlockStateModels
import net.minecraft.block.Block

class BlockGloomrockSmoothStairs(fullBlock: Block) : BlockStairsCustom(fullBlock) {
	override val localization
		get() = LocalizationStrategy.MoveToBeginning(wordCount = 1, wordOffset = 1)
	
	override val model
		get() = BlockStateModels.Stairs(fullBlock, side = Resource.Custom("block/gloomrock_smooth_slab_side"))
}
