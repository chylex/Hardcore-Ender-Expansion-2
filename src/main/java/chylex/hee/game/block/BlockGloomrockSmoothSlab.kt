package chylex.hee.game.block

import chylex.hee.game.Resource
import chylex.hee.game.block.properties.BlockStateModels
import net.minecraft.block.Block
import net.minecraft.block.SlabBlock

open class BlockGloomrockSmoothSlab(private val fullBlock: Block) : SlabBlock(Properties.from(fullBlock)), IHeeBlock {
	override val model
		get() = BlockStateModels.Slab(fullBlock, side = Resource.Custom("block/gloomrock_smooth_slab_side"))
}
