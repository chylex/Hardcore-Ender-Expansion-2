package chylex.hee.game.item

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.entity.IHeeMobEntityType
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.init.ModEntities
import chylex.hee.util.color.IntColor
import net.minecraft.entity.EntityType
import net.minecraft.item.SpawnEggItem

@Suppress("RedundantRequireNotNullCall")
open class ItemSpawnEgg(private val entityType: EntityType<*>, primaryColor: IntColor, secondaryColor: IntColor, builder: Properties) : SpawnEggItem(checkNotNull(entityType), primaryColor.i, secondaryColor.i, builder), IHeeItem {
	override val localization
		get() = (ModEntities.getHeeType(entityType) as? IHeeMobEntityType<*>)?.spawnEggName?.let { LocalizationStrategy.Custom("$it Spawn Egg") } ?: super.localization
	
	override val model
		get() = ItemModel.SpawnEgg
}
