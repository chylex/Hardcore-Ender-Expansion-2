package chylex.hee.game.mechanics.potion.brewing.recipes
import chylex.hee.game.mechanics.potion.brewing.IBrewingRecipe
import chylex.hee.game.mechanics.potion.brewing.IBrewingRecipe.Companion.checkPotion
import chylex.hee.game.mechanics.potion.brewing.PotionBrewing
import net.minecraft.init.PotionTypes.AWKWARD
import net.minecraft.init.PotionTypes.WATER
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionType

sealed class BrewBasicEffects(private val base: PotionType, private val registry: Map<Pair<Item, Int>, Potion>) : IBrewingRecipe{
	override fun isInput(input: ItemStack): Boolean{
		return checkPotion(input, base)
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean{
		return registry.keys.any { matchesReagent(ingredient, it) }
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack{
		val entry = registry.entries.first { matchesReagent(ingredient, it.key) }
		return PotionBrewing.INFO[entry.value]?.getBasePotion(input.item) ?: ItemStack.EMPTY
	}
	
	private fun matchesReagent(ingredient: ItemStack, key: Pair<Item, Int>): Boolean{
		return ingredient.item === key.first && ingredient.metadata == key.second
	}
	
	object FromWater : BrewBasicEffects(WATER, PotionBrewing.WATER)
	object FromAwkward : BrewBasicEffects(AWKWARD, PotionBrewing.AWKWARD)
}
