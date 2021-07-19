package chylex.hee.game.item

import chylex.hee.game.item.properties.ItemTint

interface IHeeItem {
	val tint: ItemTint?
		get() = null
}
