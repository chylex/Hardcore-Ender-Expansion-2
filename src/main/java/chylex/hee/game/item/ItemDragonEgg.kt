package chylex.hee.game.item

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.entity.item.EntityItemNoBob
import chylex.hee.game.item.builder.HeeBlockItemBuilder
import chylex.hee.game.item.components.IItemEntityComponent
import chylex.hee.game.item.properties.ItemModel
import net.minecraft.block.Block

class ItemDragonEgg(block: Block) : HeeBlockItemBuilder(block) {
	init {
		localization = LocalizationStrategy.None
		model = ItemModel.Manual
		
		components.itemEntity = IItemEntityComponent.fromConstructor(::EntityItemNoBob)
	}
}
