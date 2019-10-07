package chylex.hee.game.mechanics.potion.brewing
import chylex.hee.game.mechanics.potion.PotionPurity
import chylex.hee.game.mechanics.potion.brewing.modifiers.BrewConvertBottle
import chylex.hee.game.mechanics.potion.brewing.modifiers.BrewIncreaseDuration
import chylex.hee.game.mechanics.potion.brewing.modifiers.BrewIncreaseLevel
import chylex.hee.game.mechanics.potion.brewing.modifiers.BrewReversal
import chylex.hee.system.migration.vanilla.PotionTypes
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.nbtOrNull
import net.minecraft.item.Item
import net.minecraft.item.ItemPotion
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionType
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
	
	private val TYPE_MAPPING = mapOf(
		Potions.NIGHT_VISION    to PotionTypes.NIGHT_VISION,
		Potions.INVISIBILITY    to PotionTypes.INVISIBILITY,
		Potions.JUMP_BOOST      to PotionTypes.LEAPING,
		Potions.FIRE_RESISTANCE to PotionTypes.FIRE_RESISTANCE,
		Potions.SPEED           to PotionTypes.SWIFTNESS,
		Potions.SLOWNESS        to PotionTypes.SLOWNESS,
		Potions.WATER_BREATHING to PotionTypes.WATER_BREATHING,
		Potions.INSTANT_HEALTH  to PotionTypes.HEALING,
		Potions.INSTANT_DAMAGE  to PotionTypes.HARMING,
		Potions.POISON          to PotionTypes.POISON,
		Potions.REGENERATION    to PotionTypes.REGENERATION,
		Potions.STRENGTH        to PotionTypes.STRENGTH,
		Potions.WEAKNESS        to PotionTypes.WEAKNESS,
		PotionPurity            to PotionPurity.TYPE
	)
	
	private val TYPE_NO_EFFECT_OVERRIDES = mutableMapOf<PotionType, PotionType>()
	
	val ALTERED_TYPES
		get() = TYPE_MAPPING.values
	
	fun registerNoEffectOverride(original: PotionType, override: PotionType){
		TYPE_NO_EFFECT_OVERRIDES[original] = override
	}
	
	fun findNoEffectOverride(type: PotionType): PotionType{
		return TYPE_NO_EFFECT_OVERRIDES[type] ?: type
	}
	
	fun getBottle(item: Item, type: PotionType): ItemStack{
		return PotionUtils.addPotionToItemStack(ItemStack(item), type)
	}
	
	fun getBottle(item: Item, potion: Potion, withBaseEffect: Boolean): ItemStack{
		val type = TYPE_MAPPING.getOrDefault(potion, PotionTypes.WATER)
		
		return if (withBaseEffect)
			getBottle(item, type)
		else
			getBottle(item, findNoEffectOverride(type))
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
