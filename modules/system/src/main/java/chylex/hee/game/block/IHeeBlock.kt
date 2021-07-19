package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer
import chylex.hee.game.block.properties.BlockRenderLayer.SOLID
import chylex.hee.game.block.properties.BlockTint
import chylex.hee.game.block.properties.IBlockStateModel

interface IHeeBlock {
	val model: IBlockStateModel
		get() = BlockModel.Cube
	
	val renderLayer: BlockRenderLayer
		get() = SOLID
	
	val tint: BlockTint?
		get() = null
}
