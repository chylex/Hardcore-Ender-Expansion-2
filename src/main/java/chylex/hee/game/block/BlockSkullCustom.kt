package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.item.properties.ItemModel
import net.minecraft.block.Blocks
import net.minecraft.block.SkullBlock
import net.minecraft.block.WallSkullBlock

class BlockSkullCustom(type: ISkullType, builder: BlockBuilder) : SkullBlock(type, builder.p), IHeeBlock {
	override val model
		get() = BlockStateModel(BlockStatePreset.SimpleFrom(Blocks.SOUL_SAND), BlockModel.Manual, ItemModel.Skull)
	
	class Wall(type: ISkullType, builder: BlockBuilder) : WallSkullBlock(type, builder.p), IHeeBlock {
		override val model
			get() = BlockStateModel(BlockStatePreset.SimpleFrom(Blocks.SOUL_SAND), BlockModel.Manual)
	}
}
