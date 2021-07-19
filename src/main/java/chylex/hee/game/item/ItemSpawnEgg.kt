package chylex.hee.game.item

import chylex.hee.game.item.properties.ItemModel
import chylex.hee.util.color.IntColor
import net.minecraft.entity.EntityType
import net.minecraft.item.SpawnEggItem

class ItemSpawnEgg(entityType: EntityType<*>, primaryColor: IntColor, secondaryColor: IntColor, builder: Properties) : SpawnEggItem(entityType, primaryColor.i, secondaryColor.i, builder), IHeeItem {
	override val model
		get() = ItemModel.SpawnEgg
}
