package chylex.hee.game.item
import net.minecraft.block.BlockSlab
import net.minecraft.item.ItemSlab
import net.minecraft.item.ItemStack

class ItemBlockSlabFuel(half: BlockSlab, full: BlockSlab, private val burnTicks: Int) : ItemSlab(half, half, full){
	override fun getItemBurnTime(stack: ItemStack): Int{
		return burnTicks
	}
}
