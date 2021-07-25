package chylex.hee.game.item

import chylex.hee.game.item.properties.ItemModel
import chylex.hee.util.color.IntColor
import net.minecraft.entity.EntityType
import net.minecraft.item.SpawnEggItem

@Suppress("RedundantRequireNotNullCall")
class ItemSpawnEgg(entityType: EntityType<*>, primaryColor: IntColor, secondaryColor: IntColor, builder: Properties) : SpawnEggItem(checkNotNull(entityType), primaryColor.i, secondaryColor.i, builder), IHeeItem {
	override val model
		get() = ItemModel.SpawnEgg
}
