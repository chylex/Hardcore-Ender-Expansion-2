package chylex.hee.game.potion.brewing
import chylex.hee.game.inventory.nbtOrNull
import chylex.hee.game.potion.brewing.modifiers.BrewConvertBottle
import chylex.hee.game.potion.brewing.modifiers.BrewIncreaseDuration
import chylex.hee.game.potion.brewing.modifiers.BrewIncreaseLevel
import chylex.hee.game.potion.brewing.modifiers.BrewReversal
import chylex.hee.system.migration.ItemPotion
import chylex.hee.system.migration.Potion
import chylex.hee.system.migration.PotionType
import chylex.hee.system.serialization.hasKey
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionUtils
import net.minecraftforge.common.brewing.BrewingRecipeRegistry

object PotionItems{
	const val CUSTOM_EFFECTS_TAG = "CustomPotionEffects"
	
	private val MODIFIERS = arrayOf(
		BrewIncreaseDuration,
		BrewIncreaseLevel,
		BrewConvertBottle.IntoSplash,
		BrewConvertBottle.IntoLingering,
		BrewReversal
	).associateBy { it.ingredient }
	
	fun getBottle(item: Item, type: PotionType): ItemStack{
		return PotionUtils.addPotionToItemStack(ItemStack(item), type)
	}
	
	fun getBottle(item: Item, potion: Potion, withBaseEffect: Boolean): ItemStack{
		val type = PotionTypeMap.getTypeOrWater(potion)
		
		return if (withBaseEffect)
			getBottle(item, type)
		else
			getBottle(item, PotionTypeMap.findNoEffectOverride(type))
	}
	
	fun checkBottle(stack: ItemStack, type: PotionType): Boolean{
		return isPotion(stack) && PotionUtils.getPotionFromItem(stack) === type && !stack.nbtOrNull.hasKey(CUSTOM_EFFECTS_TAG)
	}
	
	fun isPotion(stack: ItemStack): Boolean{
		return stack.item is ItemPotion
	}
	
	fun isReagent(stack: ItemStack): Boolean{
		return BrewingRecipeRegistry.isValidIngredient(stack)
	}
	
	fun isModifier(ingredient: ItemStack): Boolean{
		return MODIFIERS.containsKey(ingredient.item)
	}
	
	fun findModifier(ingredient: ItemStack): IBrewingModifier?{
		return MODIFIERS[ingredient.item]
	}
}
