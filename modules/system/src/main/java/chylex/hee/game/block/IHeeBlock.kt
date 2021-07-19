package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockRenderLayer
import chylex.hee.game.block.properties.BlockRenderLayer.SOLID
import chylex.hee.game.block.properties.BlockTint

interface IHeeBlock {
	val renderLayer: BlockRenderLayer
		get() = SOLID
	
	val tint: BlockTint?
		get() = null
}
