package chylex.hee.game.mechanics.potion.brewing.modifiers
import chylex.hee.game.mechanics.potion.brewing.IBrewingModifier
import chylex.hee.game.mechanics.potion.brewing.PotionBrewing
import chylex.hee.system.util.nbtOrNull
import chylex.hee.system.util.size
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

sealed class BrewConvertBottle(override val ingredient: Item, private val newContainer: Item) : IBrewingModifier{
	override fun check(input: ItemStack): Boolean{
		return input.item === Items.POTIONITEM && PotionBrewing.unpack(input) != null
	}
	
	override fun apply(input: ItemStack): ItemStack{
		return ItemStack(newContainer, input.size, input.metadata).also {
			it.tagCompound = input.nbtOrNull?.copy()
		}
	}
	
	object IntoSplash : BrewConvertBottle(Items.GUNPOWDER, Items.SPLASH_POTION)
	object IntoLingering : BrewConvertBottle(Items.DRAGON_BREATH, Items.LINGERING_POTION)
}
