package chylex.hee.game.potion.brewing
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

interface IBrewingModifier{
	val ingredient: Item
	
	fun check(input: ItemStack): Boolean
	fun apply(input: ItemStack): ItemStack
}
