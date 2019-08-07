package chylex.hee.game.mechanics.potion.brewing
import chylex.hee.game.mechanics.potion.PotionPurity.PURITY
import chylex.hee.game.mechanics.potion.brewing.PotionTypeInfo.Duration
import chylex.hee.init.ModItems.DRAGON_SCALE
import chylex.hee.system.util.floorToInt
import net.minecraft.init.Items.BLAZE_POWDER
import net.minecraft.init.Items.FERMENTED_SPIDER_EYE
import net.minecraft.init.Items.FISH
import net.minecraft.init.Items.GHAST_TEAR
import net.minecraft.init.Items.GOLDEN_CARROT
import net.minecraft.init.Items.MAGMA_CREAM
import net.minecraft.init.Items.RABBIT_FOOT
import net.minecraft.init.Items.SPECKLED_MELON
import net.minecraft.init.Items.SPIDER_EYE
import net.minecraft.init.Items.SUGAR
import net.minecraft.init.MobEffects.BLINDNESS
import net.minecraft.init.MobEffects.FIRE_RESISTANCE
import net.minecraft.init.MobEffects.GLOWING
import net.minecraft.init.MobEffects.INSTANT_DAMAGE
import net.minecraft.init.MobEffects.INSTANT_HEALTH
import net.minecraft.init.MobEffects.INVISIBILITY
import net.minecraft.init.MobEffects.JUMP_BOOST
import net.minecraft.init.MobEffects.LEVITATION
import net.minecraft.init.MobEffects.NIGHT_VISION
import net.minecraft.init.MobEffects.POISON
import net.minecraft.init.MobEffects.REGENERATION
import net.minecraft.init.MobEffects.SLOWNESS
import net.minecraft.init.MobEffects.SPEED
import net.minecraft.init.MobEffects.STRENGTH
import net.minecraft.init.MobEffects.WATER_BREATHING
import net.minecraft.init.MobEffects.WEAKNESS
import net.minecraft.item.ItemFishFood.FishType.PUFFERFISH
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionUtils

object PotionBrewing{
	val INFO = arrayOf(
		PotionTypeInfo(INSTANT_HEALTH,  maxLevel = 2),
		PotionTypeInfo(FIRE_RESISTANCE, Duration(baseTicks = 3 min 20, stepTicks = 3 min 10,    maxSteps = 4), maxLevel = 1),
		PotionTypeInfo(REGENERATION,    Duration(baseTicks = 0 min 30, stepTicks = 0 min 30,    maxSteps = 3), maxLevel = 3),
		PotionTypeInfo(STRENGTH,        Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(SPEED,           Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(NIGHT_VISION,    Duration(baseTicks = 3 min 20, stepTicks = 3 min 10,    maxSteps = 4), maxLevel = 1),
		PotionTypeInfo(WATER_BREATHING, Duration(baseTicks = 3 min 20, stepTicks = 3 min 10,    maxSteps = 4), maxLevel = 1),
		PotionTypeInfo(JUMP_BOOST,      Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(POISON,          Duration(baseTicks = 0 min 30, stepTicks = 0 min 30,    maxSteps = 3), maxLevel = 2),
//		PotionTypeInfo(TURTLE_MASTER,   Duration(baseTicks = 0 min 20, stepTicks = 0 min 13.34, maxSteps = 3), maxLevel = 2), // UPDATE
//		PotionTypeInfo(SLOW_FALLING,    Duration(baseTicks = 1 min 15, stepTicks = 1 min 15,    maxSteps = 3), maxLevel = 1), // UPDATE
		PotionTypeInfo(LEVITATION,      Duration(baseTicks = 0 min 30, stepTicks = 0 min 30,    maxSteps = 3), maxLevel = 2),
		PotionTypeInfo(PURITY,          Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
//		PotionTypeInfo(CORRUPTION,      Duration(baseTicks = 0 min 20, stepTicks = 0 min 13.34, maxSteps = 3), maxLevel = 1), // TODO
		PotionTypeInfo(BLINDNESS,       Duration(baseTicks = 0 min 20, stepTicks = 0 min 13.34, maxSteps = 3), maxLevel = 1),
		PotionTypeInfo(WEAKNESS,        Duration(baseTicks = 1 min 15, stepTicks = 1 min 15,    maxSteps = 3), maxLevel = 3),
		
		PotionTypeInfo(INSTANT_DAMAGE,  maxLevel = 2),
		PotionTypeInfo(SLOWNESS,        Duration(baseTicks = 1 min 15, stepTicks = 1 min 7.5, maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(INVISIBILITY,    Duration(baseTicks = 1 min 20, stepTicks = 1 min 16,  maxSteps = 4), maxLevel = 1),
//		PotionTypeInfo(BANISHMENT,      Duration(baseTicks = 0 min 15, stepTicks = 0 min 10,  maxSteps = 3), maxLevel = 1), // TODO
		PotionTypeInfo(GLOWING,         Duration(baseTicks = 0 min 12, stepTicks = 0 min 8,   maxSteps = 3), maxLevel = 1)
	).associateBy { it.potion }
	
	// TODO register levitation type and other custom potions
	
	val AWKWARD = mapOf(
		(SPECKLED_MELON to 0)         to INSTANT_HEALTH,
		(MAGMA_CREAM to 0)            to FIRE_RESISTANCE,
		(GHAST_TEAR to 0)             to REGENERATION,
		(BLAZE_POWDER to 0)           to STRENGTH,
		(SUGAR to 0)                  to SPEED,
		(GOLDEN_CARROT to 0)          to NIGHT_VISION,
		(FISH to PUFFERFISH.metadata) to WATER_BREATHING, // UPDATE
		(RABBIT_FOOT to 0)            to JUMP_BOOST,
		(SPIDER_EYE to 0)             to POISON,
		// UPDATE (TURTLE_SHELL to 0)           to TURTLE_MASTER,
		// UPDATE (PHANTOM_MEMBRANE to 0)       to SLOW_FALLING,
		(DRAGON_SCALE to 0)           to LEVITATION
		// TODO (PURITY_EXTRACT to 0)         to PURITY,
		// TODO (INSTABILITY_ORB to 0)        to CORRUPTION,
		// TODO (MURKY_CRYSTAL to 0)          to BLINDNESS
	)
	
	val WATER = mapOf(
		(FERMENTED_SPIDER_EYE to 0) to WEAKNESS
	)
	
	val REVERSAL = mapOf(
		INSTANT_HEALTH to INSTANT_DAMAGE,
		POISON         to INSTANT_DAMAGE,
		SPEED          to SLOWNESS,
		JUMP_BOOST     to SLOWNESS,
		NIGHT_VISION   to INVISIBILITY,
		// TODO CORRUPTION     to BANISHMENT,
		BLINDNESS      to GLOWING
	)
	
	fun unpack(stack: ItemStack): PotionTypeInfo.Instance?{
		val effects = PotionUtils.getEffectsFromStack(stack)
		
		if (effects.size != 1){
			return null
		}
		
		val effect = effects[0]
		val potion = effect.potion
		
		return INFO[potion]?.Instance(stack, effect)
	}
	
	fun isAltered(stack: ItemStack): Boolean{
		return unpack(stack) != null || (PotionItems.isPotion(stack) && PotionUtils.getEffectsFromStack(stack).isEmpty())
	}
	
	// Utilities
	
	private infix fun Int.min(seconds: Int): Int{
		return ((this * 60) + seconds) * 20
	}
	
	private infix fun Int.min(seconds: Double): Int{
		return (((this * 60) + seconds) * 20).floorToInt()
	}
}
