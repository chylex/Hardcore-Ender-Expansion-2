package chylex.hee.game.item

import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.properties.ItemTint
import chylex.hee.game.item.util.ItemProperty
import net.minecraft.item.Item
import net.minecraft.tags.ITag.INamedTag

interface IHeeItem {
	val model: ItemModel
		get() = ItemModel.Simple
	
	val tint: ItemTint?
		get() = null
	
	val properties: List<ItemProperty>
		get() = emptyList()
	
	val tags: List<INamedTag<Item>>
		get() = emptyList()
}
