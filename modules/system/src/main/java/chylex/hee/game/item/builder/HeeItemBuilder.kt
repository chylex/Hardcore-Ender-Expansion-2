package chylex.hee.game.item.builder

import chylex.hee.game.item.HeeItem
import chylex.hee.game.item.HeeItemWithComponents
import chylex.hee.game.item.IHeeItem
import chylex.hee.game.item.interfaces.IItemWithInterfaces
import net.minecraft.item.Item.Properties

open class HeeItemBuilder : AbstractHeeItemBuilder<HeeItem>() {
	override fun buildItem(properties: Properties, components: HeeItemComponents?): HeeItem {
		return if (components != null)
			object : HeeItemWithComponents(properties, components), IHeeItem by IHeeItem.FromBuilder(this), IItemWithInterfaces by interfaces.delegate {}
		else
			object : HeeItem(properties), IHeeItem by IHeeItem.FromBuilder(this), IItemWithInterfaces by interfaces.delegate {}
	}
}

inline fun HeeItemBuilder(setup: HeeItemBuilder.() -> Unit) = HeeItemBuilder().apply(setup)
