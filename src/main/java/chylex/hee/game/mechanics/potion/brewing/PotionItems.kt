package chylex.hee.game.mechanics.potion.brewing
import net.minecraft.init.MobEffects
import net.minecraft.init.PotionTypes
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion

object PotionItems{
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
	
	fun get(item: Item, potion: Potion): ItemStack{
		return IBrewingRecipe.getPotion(item, TYPE_MAPPING.getOrDefault(potion, PotionTypes.WATER))
	}
}
