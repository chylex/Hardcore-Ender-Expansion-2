package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.block.properties.CustomSkull.ICustomSkull
import chylex.hee.game.item.properties.ItemModel
import net.minecraft.block.Blocks
import net.minecraft.block.SkullBlock
import net.minecraft.block.WallSkullBlock

class BlockSkullCustom(private val type: ICustomSkull, builder: BlockBuilder) : SkullBlock(type, builder.p), IHeeBlock {
	override val localization
		get() = LocalizationStrategy.None
	
	override val model
		get() = BlockStateModel(BlockStatePreset.SimpleFrom(Blocks.SOUL_SAND), BlockModel.Manual, ItemModel.Skull)
	
	override val drop
		get() = BlockDrop.OneOf(type.asItem())
	
	class Wall(private val type: ICustomSkull, builder: BlockBuilder) : WallSkullBlock(type, builder.p), IHeeBlock {
		override val localization
			get() = LocalizationStrategy.None
		
		override val model
			get() = BlockStateModel(BlockStatePreset.SimpleFrom(Blocks.SOUL_SAND), BlockModel.Manual)
		
		override val drop
			get() = BlockDrop.OneOf(type.asItem())
	}
}
