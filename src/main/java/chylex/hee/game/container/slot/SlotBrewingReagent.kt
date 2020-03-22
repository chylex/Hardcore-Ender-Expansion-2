package chylex.hee.game.container.slot
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.init.ModItems
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack

class SlotBrewingReagent(wrapped: Slot, private val isEnhanced: Boolean) : SlotWrapper(wrapped){
	override fun isItemValid(stack: ItemStack): Boolean{
		return TileEntityBrewingStandCustom.canInsertIntoReagentSlot(stack, isEnhanced)
	}
}
