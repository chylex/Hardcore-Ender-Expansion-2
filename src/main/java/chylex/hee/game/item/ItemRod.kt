package chylex.hee.game.item

import net.minecraftforge.common.Tags

class ItemRod(properties: Properties) : HeeItem(properties) {
	override val tags
		get() = listOf(Tags.Items.RODS)
}
