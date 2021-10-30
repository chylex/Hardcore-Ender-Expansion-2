package chylex.hee.game.item

import chylex.hee.game.entity.item.EntityItemIgneousRock
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.IItemEntityComponent

object ItemIgneousRock : HeeItemBuilder() {
	init {
		components.itemEntity = IItemEntityComponent.fromConstructor(::EntityItemIgneousRock)
		components.furnaceBurnTime = 1300
	}
}
