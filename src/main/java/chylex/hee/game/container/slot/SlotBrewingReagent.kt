package chylex.hee.game.container.slot
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import chylex.hee.init.ModItems
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

class SlotBrewingReagent(wrapped: Slot, private val allowEndPowder: Boolean) : SlotWrapper(wrapped){
	override fun isItemValid(stack: ItemStack): Boolean{
		return if (stack.item === ModItems.END_POWDER)
			allowEndPowder
		else
			PotionItems.isReagent(stack)
	}
}
