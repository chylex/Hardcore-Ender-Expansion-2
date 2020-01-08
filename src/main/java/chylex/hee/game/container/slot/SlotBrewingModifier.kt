package chylex.hee.game.container.slot
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack

class SlotBrewingModifier(wrapped: Slot) : SlotWrapper(wrapped){
	override fun isItemValid(stack: ItemStack) = PotionItems.isModifier(stack)
}
