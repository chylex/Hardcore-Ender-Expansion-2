package chylex.hee.game.potion.brewing.recipes

import chylex.hee.game.potion.brewing.IBrewingRecipe
import chylex.hee.game.potion.brewing.PotionBrewing
import chylex.hee.game.potion.brewing.PotionItems
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.potion.Effect
import net.minecraft.potion.Potion
import net.minecraft.potion.Potions

sealed class BrewBasicEffects(private val base: Potion, private val registry: Map<Item, Effect>) : IBrewingRecipe {
	override fun isInput(input: ItemStack): Boolean {
		return PotionItems.checkBottle(input, base)
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean {
		return registry.keys.any { matchesReagent(ingredient, it) }
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack {
		val entry = registry.entries.first { matchesReagent(ingredient, it.key) }
		val info = PotionBrewing.INFO[entry.value] ?: return ItemStack.EMPTY
		
		return PotionItems.getBottle(input.item, info.effect, withBaseEffect = true)
	}
	
	private fun matchesReagent(ingredient: ItemStack, item: Item): Boolean {
		return ingredient.item === item
	}
	
	object FromWater : BrewBasicEffects(Potions.WATER, PotionBrewing.WATER)
	object FromAwkward : BrewBasicEffects(Potions.AWKWARD, PotionBrewing.AWKWARD)
}
