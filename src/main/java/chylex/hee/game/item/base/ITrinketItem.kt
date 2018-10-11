package chylex.hee.game.item.base
import net.minecraft.item.ItemStack

interface ITrinketItem{
	fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean = true
}
