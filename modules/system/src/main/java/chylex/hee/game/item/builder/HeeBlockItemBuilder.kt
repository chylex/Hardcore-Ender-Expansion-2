package chylex.hee.game.item.builder

import chylex.hee.game.item.HeeBlockItem
import chylex.hee.game.item.HeeBlockItemWithComponents
import chylex.hee.game.item.IHeeItem
import chylex.hee.game.item.interfaces.IItemWithInterfaces
import net.minecraft.block.Block
import net.minecraft.item.Item.Properties

open class HeeBlockItemBuilder(private val block: Block) : AbstractHeeItemBuilder<HeeBlockItem>() {
	override fun buildItem(properties: Properties, components: HeeItemComponents?): HeeBlockItem {
		return if (components != null)
			object : HeeBlockItemWithComponents(block, properties, components), IHeeItem by IHeeItem.FromBuilder(this), IItemWithInterfaces by interfaces.delegate {}
		else
			object : HeeBlockItem(block, properties), IHeeItem by IHeeItem.FromBuilder(this), IItemWithInterfaces by interfaces.delegate {}
	}
}

inline fun HeeBlockItemBuilder(block: Block, setup: HeeBlockItemBuilder.() -> Unit) = HeeBlockItemBuilder(block).apply(setup)
