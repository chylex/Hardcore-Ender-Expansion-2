package chylex.hee.game.item

import chylex.hee.game.item.builder.HeeBlockItemBuilder
import net.minecraft.block.Block

class ItemInfusedTNT(block: Block) : HeeBlockItemBuilder(block) {
	init {
		includeFrom(ItemAbstractInfusable())
	}
}
