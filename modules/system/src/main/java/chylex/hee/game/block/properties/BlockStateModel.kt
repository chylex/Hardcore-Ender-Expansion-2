package chylex.hee.game.block.properties

import chylex.hee.game.item.properties.ItemModel

class BlockStateModel(
	override val blockState: BlockStatePreset,
	override val blockModel: BlockModel,
	override val itemModel: BlockItemModel?,
) : IBlockStateModel {
	constructor(blockState: BlockStatePreset, blockModel: BlockModel, itemModel: ItemModel?) : this(blockState, blockModel, itemModel?.let(::BlockItemModel))
	constructor(blockState: BlockStatePreset, blockModel: BlockModel) : this(blockState, blockModel, null as BlockItemModel?)
}
