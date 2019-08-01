package chylex.hee.game.mechanics.potion.brewing
import chylex.hee.game.mechanics.potion.brewing.modifiers.BrewConvertBottle
import chylex.hee.game.mechanics.potion.brewing.modifiers.BrewIncreaseDuration
import chylex.hee.game.mechanics.potion.brewing.modifiers.BrewIncreaseLevel
import chylex.hee.game.mechanics.potion.brewing.modifiers.BrewReversal
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.nbtOrNull
import net.minecraft.init.MobEffects
import net.minecraft.init.PotionTypes
import net.minecraft.item.Item
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
		MobEffects.NIGHT_VISION    to PotionTypes.NIGHT_VISION,
		MobEffects.INVISIBILITY    to PotionTypes.INVISIBILITY,
		MobEffects.JUMP_BOOST      to PotionTypes.LEAPING,
		MobEffects.FIRE_RESISTANCE to PotionTypes.FIRE_RESISTANCE,
		MobEffects.SPEED           to PotionTypes.SWIFTNESS,
		MobEffects.SLOWNESS        to PotionTypes.SLOWNESS,
		MobEffects.WATER_BREATHING to PotionTypes.WATER_BREATHING,
		MobEffects.INSTANT_HEALTH  to PotionTypes.HEALING,
		MobEffects.INSTANT_DAMAGE  to PotionTypes.HARMING,
		MobEffects.POISON          to PotionTypes.POISON,
		MobEffects.REGENERATION    to PotionTypes.REGENERATION,
		MobEffects.STRENGTH        to PotionTypes.STRENGTH,
		MobEffects.WEAKNESS        to PotionTypes.WEAKNESS
	)
	
	val VANILLA_TYPES
		get() = TYPE_MAPPING.values
	
	fun checkBottle(stack: ItemStack, type: PotionType): Boolean{
		return PotionUtils.getPotionFromItem(stack) === type && !stack.nbtOrNull.hasKey(CUSTOM_EFFECTS_TAG)
	}
	
	fun getBottle(item: Item, type: PotionType): ItemStack{
		return PotionUtils.addPotionToItemStack(ItemStack(item), type)
	}
	
	fun getBottle(item: Item, potion: Potion): ItemStack{
		return getBottle(item, TYPE_MAPPING.getOrDefault(potion, PotionTypes.WATER))
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
