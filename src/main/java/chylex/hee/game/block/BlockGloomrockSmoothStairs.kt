package chylex.hee.game.block

import chylex.hee.game.Resource
import chylex.hee.game.block.properties.BlockStateModels
import net.minecraft.block.Block

class BlockGloomrockSmoothStairs(fullBlock: Block) : BlockStairsCustom(fullBlock) {
	override val model
		get() = BlockStateModels.Stairs(fullBlock, side = Resource.Custom("block/gloomrock_smooth_slab_side"))
}
