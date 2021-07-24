package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer
import chylex.hee.game.block.properties.BlockRenderLayer.SOLID
import chylex.hee.game.block.properties.BlockTint
import chylex.hee.game.block.properties.IBlockStateModel
import net.minecraft.block.Block
import net.minecraft.tags.ITag.INamedTag

interface IHeeBlock {
	val model: IBlockStateModel
		get() = BlockModel.Cube
	
	val renderLayer: BlockRenderLayer
		get() = SOLID
	
	val tint: BlockTint?
		get() = null
	
	val drop: BlockDrop
		get() = BlockDrop.Self
	
	val tags: List<INamedTag<Block>>
		get() = emptyList()
}
