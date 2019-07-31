package chylex.hee.game.mechanics.potion.brewing
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.nbtOrNull
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionType
import net.minecraft.potion.PotionUtils

interface IBrewingRecipe : net.minecraftforge.common.brewing.IBrewingRecipe{
	@JvmDefault
	override fun getOutput(input: ItemStack, ingredient: ItemStack): ItemStack{
		return if (isIngredient(ingredient) && isInput(input))
			brew(input, ingredient)
		else
			ItemStack.EMPTY
	}
	
	fun brew(input: ItemStack, ingredient: ItemStack): ItemStack
	
	companion object{
		const val CUSTOM_EFFECTS_TAG = "CustomPotionEffects"
		
		fun checkPotion(stack: ItemStack, type: PotionType): Boolean{
			return PotionUtils.getPotionFromItem(stack) === type && !stack.nbtOrNull.hasKey(CUSTOM_EFFECTS_TAG)
		}
		
		fun getPotion(item: Item, type: PotionType): ItemStack{
			return PotionUtils.addPotionToItemStack(ItemStack(item), type)
		}
	}
}
