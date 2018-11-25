package chylex.hee.game.item.base
import net.minecraft.item.ItemStack

/**
 * Describes an item which can be inserted into a Trinket slot. The interface must be applied to a class extending [Item][net.minecraft.item.Item].
 */
interface ITrinketItem{
	/**
	 * Returns true if the Trinket can be activated, i.e. it can be inserted into a Trinket slot and is then returned by [TrinketHandler.getCurrentActiveItem][chylex.hee.game.mechanics.TrinketHandler.getCurrentActiveItem].
	 */
	@JvmDefault fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean = true
}
