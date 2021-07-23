package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.item.properties.ItemModel
import net.minecraft.block.Block
import net.minecraft.block.Blocks

class BlockEndersol(builder: BlockBuilder, mergeBottom: Block) : BlockSimpleMergingBottom(builder, mergeBottom) {
	override val model
		get() = BlockStateModel(
			BlockStatePreset.None,
			BlockModel.Multi(
				BlockModel.CubeColumn,
				BlockModel.Suffixed("_merge_1", BlockModel.CubeBottomTop(side = this.location("_merge_1"), bottom = Blocks.END_STONE.location, top = this.location("_top"))),
				BlockModel.Suffixed("_merge_2", BlockModel.CubeBottomTop(side = this.location("_merge_2"), bottom = Blocks.END_STONE.location, top = this.location("_top")))
			),
			ItemModel.AsBlock
		)
	
	override val drop
		get() = BlockDrop.Manual
}
