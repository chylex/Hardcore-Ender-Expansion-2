package chylex.hee.game.item

import net.minecraftforge.common.Tags

open class ItemDust(properties: Properties) : HeeItem(properties) {
	override val tags
		get() = listOf(Tags.Items.DUSTS)
}
