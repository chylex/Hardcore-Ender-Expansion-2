package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.builder.AbstractHeeBlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer
import chylex.hee.game.block.properties.BlockRenderLayer.SOLID
import chylex.hee.game.block.properties.BlockTint
import chylex.hee.game.block.properties.IBlockStateModelSupplier
import net.minecraft.block.Block
import net.minecraft.tags.ITag.INamedTag

interface IHeeBlock {
	val localization: LocalizationStrategy
		get() = LocalizationStrategy.Default
	
	val localizationExtra: Map<String, String>
		get() = emptyMap()
	
	val model: IBlockStateModelSupplier
		get() = BlockModel.Cube
	
	val renderLayer: BlockRenderLayer
		get() = SOLID
	
	val tint: BlockTint?
		get() = null
	
	val drop: BlockDrop
		get() = BlockDrop.Self
	
	val tags: List<INamedTag<Block>>
		get() = emptyList()
	
	class FromBuilder(builder: AbstractHeeBlockBuilder<*>) : IHeeBlock {
		override val localization = builder.localization ?: super.localization
		override val localizationExtra = builder.localizationExtra.toMap()
		override val model = builder.model ?: super.model
		override val renderLayer = builder.renderLayer ?: super.renderLayer
		override val tint = builder.tint
		override val drop = builder.drop ?: super.drop
		override val tags = builder.tags.toList()
	}
}
