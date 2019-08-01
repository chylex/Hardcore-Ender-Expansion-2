package chylex.hee.game.container.slot
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

class SlotBrewingReagent(wrapped: Slot) : SlotWrapper(wrapped){
	override fun isItemValid(stack: ItemStack) = PotionItems.isReagent(stack)
}
