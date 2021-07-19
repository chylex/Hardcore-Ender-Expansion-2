package chylex.hee.game.item

import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.properties.ItemTint

interface IHeeItem {
	val model: ItemModel
		get() = ItemModel.Simple
	
	val tint: ItemTint?
		get() = null
}
