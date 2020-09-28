package chylex.hee.game.potion.brewing.modifiers
import chylex.hee.game.inventory.nbtOrNull
import chylex.hee.game.inventory.size
import chylex.hee.game.potion.brewing.IBrewingModifier
import chylex.hee.game.potion.brewing.PotionBrewing
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

sealed class BrewConvertBottle(override val ingredient: Item, private val oldContainer: Item, private val newContainer: Item) : IBrewingModifier{
	override fun check(input: ItemStack): Boolean{
		return input.item === oldContainer && PotionBrewing.isAltered(input)
	}
	
	override fun apply(input: ItemStack): ItemStack{
		return ItemStack(newContainer, input.size).also {
			it.tag = input.nbtOrNull?.copy()
		}
	}
	
	object IntoSplash : BrewConvertBottle(Items.GUNPOWDER, Items.POTION, Items.SPLASH_POTION)
	object IntoLingering : BrewConvertBottle(Items.DRAGON_BREATH, Items.SPLASH_POTION, Items.LINGERING_POTION)
}
