package chylex.hee.game.item
import chylex.hee.system.migration.ItemBlock
import net.minecraft.block.Block
import net.minecraft.item.ItemStack

class ItemBlockFuel(block: Block, properties: Properties, private val burnTicks: Int) : ItemBlock(block, properties){
	override fun getBurnTime(stack: ItemStack): Int{
		return burnTicks
	}
}
