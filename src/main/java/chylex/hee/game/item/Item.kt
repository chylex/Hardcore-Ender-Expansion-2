package chylex.hee.game.item

import chylex.hee.game.item.builder.HeeItemBuilder
import net.minecraftforge.common.Tags

val ItemSimple
	get() = HeeItemBuilder()

val ItemDust
	get() = HeeItemBuilder { tags.add(Tags.Items.DUSTS) }

val ItemIngot
	get() = HeeItemBuilder { tags.add(Tags.Items.INGOTS) }

val ItemNugget
	get() = HeeItemBuilder { tags.add(Tags.Items.NUGGETS) }

val ItemRod
	get() = HeeItemBuilder { tags.add(Tags.Items.RODS) }
