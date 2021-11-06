package chylex.hee.game.block.properties

import net.minecraft.block.Block

interface IBlockStateModel : IBlockStateModelSupplier {
	val blockState: BlockStatePreset
	val blockModel: BlockModel
	val itemModel: BlockItemModel?
	
	override fun generate(block: Block): IBlockStateModel {
		return this
	}
}
