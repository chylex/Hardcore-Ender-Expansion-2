package chylex.hee.game.item

import net.minecraftforge.common.Tags

class ItemNugget(properties: Properties) : HeeItem(properties) {
	override val tags
		get() = listOf(Tags.Items.NUGGETS)
}
