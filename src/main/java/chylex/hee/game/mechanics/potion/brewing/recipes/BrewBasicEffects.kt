package chylex.hee.game.mechanics.potion.brewing.recipes
import chylex.hee.game.mechanics.potion.brewing.IBrewingRecipe
import chylex.hee.game.mechanics.potion.brewing.PotionBrewing
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import chylex.hee.system.migration.vanilla.Potion
import chylex.hee.system.migration.vanilla.PotionType
import chylex.hee.system.migration.vanilla.PotionTypes
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

sealed class BrewBasicEffects(private val base: PotionType, private val registry: Map<Item, Potion>) : IBrewingRecipe{
	override fun isInput(input: ItemStack): Boolean{
		return PotionItems.checkBottle(input, base)
	}
	
	override fun isIngredient(ingredient: ItemStack): Boolean{
		return registry.keys.any { matchesReagent(ingredient, it) }
	}
	
	override fun brew(input: ItemStack, ingredient: ItemStack): ItemStack{
		val entry = registry.entries.first { matchesReagent(ingredient, it.key) }
		val info = PotionBrewing.INFO[entry.value] ?: return ItemStack.EMPTY
		
		return PotionItems.getBottle(input.item, info.potion, withBaseEffect = true)
	}
	
	private fun matchesReagent(ingredient: ItemStack, item: Item): Boolean{
		return ingredient.item === item
	}
	
	object FromWater : BrewBasicEffects(PotionTypes.WATER, PotionBrewing.WATER)
	object FromAwkward : BrewBasicEffects(PotionTypes.AWKWARD, PotionBrewing.AWKWARD)
}
