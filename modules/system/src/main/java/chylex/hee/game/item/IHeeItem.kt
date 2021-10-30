package chylex.hee.game.item

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.item.builder.AbstractHeeItemBuilder
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.properties.ItemTint
import chylex.hee.game.item.util.ItemProperty
import net.minecraft.item.Item
import net.minecraft.tags.ITag.INamedTag

interface IHeeItem {
	val localization: LocalizationStrategy
		get() = LocalizationStrategy.Default
	
	val localizationExtra: Map<String, String>
		get() = emptyMap()
	
	val model: ItemModel
		get() = ItemModel.Simple
	
	val tint: ItemTint?
		get() = null
	
	val properties: List<ItemProperty>
		get() = emptyList()
	
	val tags: List<INamedTag<Item>>
		get() = emptyList()
	
	class FromBuilder(builder: AbstractHeeItemBuilder<*>) : IHeeItem {
		override val localization = builder.localization ?: super.localization
		override val localizationExtra = builder.localizationExtra.toMap()
		override val model = builder.model ?: super.model
		override val tint = builder.tint
		override val properties = builder.properties.toList()
		override val tags = builder.tags.toList()
	}
}
