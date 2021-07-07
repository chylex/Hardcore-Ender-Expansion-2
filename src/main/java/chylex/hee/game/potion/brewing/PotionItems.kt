package chylex.hee.game.potion.brewing

import chylex.hee.game.item.util.nbtOrNull
import chylex.hee.game.potion.brewing.modifiers.BrewConvertBottle
import chylex.hee.game.potion.brewing.modifiers.BrewIncreaseDuration
import chylex.hee.game.potion.brewing.modifiers.BrewIncreaseLevel
import chylex.hee.game.potion.brewing.modifiers.BrewReversal
import chylex.hee.util.nbt.hasKey
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.PotionItem
import net.minecraft.potion.Effect
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionUtils
import net.minecraftforge.common.brewing.BrewingRecipeRegistry

object PotionItems {
	const val CUSTOM_EFFECTS_TAG = "CustomPotionEffects"
	
	private val MODIFIERS = arrayOf(
		BrewIncreaseDuration,
		BrewIncreaseLevel,
		BrewConvertBottle.IntoSplash,
		BrewConvertBottle.IntoLingering,
		BrewReversal
	).associateBy(IBrewingModifier::ingredient)
	
	fun getBottle(item: Item, potion: Potion): ItemStack {
		return PotionUtils.addPotionToItemStack(ItemStack(item), potion)
	}
	
	fun getBottle(item: Item, effect: Effect, withBaseEffect: Boolean): ItemStack {
		val type = PotionTypeMap.getPotionOrWater(effect)
		
		return if (withBaseEffect)
			getBottle(item, type)
		else
			getBottle(item, PotionTypeMap.findNoEffectOverride(type))
	}
	
	fun checkBottle(stack: ItemStack, potion: Potion): Boolean {
		return isPotion(stack) && PotionUtils.getPotionFromItem(stack) === potion && !stack.nbtOrNull.hasKey(CUSTOM_EFFECTS_TAG)
	}
	
	fun isPotion(stack: ItemStack): Boolean {
		return stack.item is PotionItem
	}
	
	fun isReagent(stack: ItemStack): Boolean {
		return BrewingRecipeRegistry.isValidIngredient(stack)
	}
	
	fun isModifier(ingredient: ItemStack): Boolean {
		return MODIFIERS.containsKey(ingredient.item)
	}
	
	fun findModifier(ingredient: ItemStack): IBrewingModifier? {
		return MODIFIERS[ingredient.item]
	}
}
