package chylex.hee.game.mechanics.potion.brewing.modifiers
import chylex.hee.game.mechanics.potion.brewing.IBrewingModifier
import chylex.hee.game.mechanics.potion.brewing.PotionBrewing
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.nbtOrNull
import chylex.hee.system.util.size
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

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
