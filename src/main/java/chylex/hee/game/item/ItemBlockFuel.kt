package chylex.hee.game.item

import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack

class ItemBlockFuel(block: Block, properties: Properties, private val burnTicks: Int) : BlockItem(block, properties) {
	override fun getBurnTime(stack: ItemStack): Int {
		return burnTicks
	}
}
