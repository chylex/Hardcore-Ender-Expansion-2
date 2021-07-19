package chylex.hee.game.block.properties

interface IBlockStateModel {
	val blockState: BlockStatePreset
	val blockModel: BlockModel
	val itemModel: BlockItemModel?
}
