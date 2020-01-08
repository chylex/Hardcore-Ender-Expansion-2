package chylex.hee.game.mechanics.potion.brewing
import chylex.hee.game.mechanics.potion.brewing.PotionTypeInfo.Duration
import chylex.hee.init.ModItems
import chylex.hee.init.ModPotions
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.util.floorToInt
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionUtils

object PotionBrewing{
	val INFO = arrayOf(
		PotionTypeInfo(Potions.INSTANT_HEALTH,  maxLevel = 2),
		PotionTypeInfo(Potions.FIRE_RESISTANCE, Duration(baseTicks = 3 min 20, stepTicks = 3 min 10,    maxSteps = 4), maxLevel = 1),
		PotionTypeInfo(Potions.REGENERATION,    Duration(baseTicks = 0 min 30, stepTicks = 0 min 30,    maxSteps = 3), maxLevel = 3),
		PotionTypeInfo(Potions.STRENGTH,        Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(Potions.SPEED,           Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(Potions.NIGHT_VISION,    Duration(baseTicks = 3 min 20, stepTicks = 3 min 10,    maxSteps = 4), maxLevel = 1),
		PotionTypeInfo(Potions.WATER_BREATHING, Duration(baseTicks = 3 min 20, stepTicks = 3 min 10,    maxSteps = 4), maxLevel = 1),
		PotionTypeInfo(Potions.JUMP_BOOST,      Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(Potions.POISON,          Duration(baseTicks = 0 min 30, stepTicks = 0 min 30,    maxSteps = 3), maxLevel = 2),
//		PotionTypeInfo(Potions.TURTLE_MASTER,   Duration(baseTicks = 0 min 20, stepTicks = 0 min 13.34, maxSteps = 3), maxLevel = 2), // UPDATE
		PotionTypeInfo(Potions.SLOW_FALLING,    Duration(baseTicks = 1 min 15, stepTicks = 1 min 15,    maxSteps = 3), maxLevel = 1),
		PotionTypeInfo(Potions.LEVITATION,      Duration(baseTicks = 0 min 30, stepTicks = 0 min 30,    maxSteps = 3), maxLevel = 2),
		PotionTypeInfo(ModPotions.PURITY,       Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
//		PotionTypeInfo(ModPotions.CORRUPTION,   Duration(baseTicks = 0 min 20, stepTicks = 0 min 13.34, maxSteps = 3), maxLevel = 1), // TODO
		PotionTypeInfo(Potions.BLINDNESS,       Duration(baseTicks = 0 min 20, stepTicks = 0 min 13.34, maxSteps = 3), maxLevel = 1),
		PotionTypeInfo(Potions.WEAKNESS,        Duration(baseTicks = 1 min 15, stepTicks = 1 min 15,    maxSteps = 3), maxLevel = 3),
		
		PotionTypeInfo(Potions.INSTANT_DAMAGE,  maxLevel = 2),
		PotionTypeInfo(Potions.SLOWNESS,        Duration(baseTicks = 1 min 15, stepTicks = 1 min 7.5, maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(Potions.INVISIBILITY,    Duration(baseTicks = 1 min 20, stepTicks = 1 min 16,  maxSteps = 4), maxLevel = 1),
//		PotionTypeInfo(ModPotions.BANISHMENT,   Duration(baseTicks = 0 min 15, stepTicks = 0 min 10,  maxSteps = 3), maxLevel = 1), // TODO
		PotionTypeInfo(Potions.GLOWING,         Duration(baseTicks = 0 min 12, stepTicks = 0 min 8,   maxSteps = 3), maxLevel = 1)
	).associateBy { it.potion }
	
	// TODO register levitation type and other custom potions
	
	val AWKWARD = mapOf(
		Items.GLISTERING_MELON_SLICE to Potions.INSTANT_HEALTH,
		Items.MAGMA_CREAM            to Potions.FIRE_RESISTANCE,
		Items.GHAST_TEAR             to Potions.REGENERATION,
		Items.BLAZE_POWDER           to Potions.STRENGTH,
		Items.SUGAR                  to Potions.SPEED,
		Items.GOLDEN_CARROT          to Potions.NIGHT_VISION,
		Items.PUFFERFISH             to Potions.WATER_BREATHING,
		Items.RABBIT_FOOT            to Potions.JUMP_BOOST,
		Items.SPIDER_EYE             to Potions.POISON,
		// UPDATE Items.SCUTE                  to Potions.TURTLE_MASTER,
		Items.PHANTOM_MEMBRANE       to Potions.SLOW_FALLING,
		ModItems.DRAGON_SCALE        to Potions.LEVITATION,
		ModItems.PURITY_EXTRACT      to ModPotions.PURITY
		// TODO (ModItems.INSTABILITY_ORB to 0)     to ModPotions.CORRUPTION,
		// TODO (ModItems.MURKY_CRYSTAL to 0)       to Potions.BLINDNESS
	)
	
	val WATER = mapOf(
		Items.FERMENTED_SPIDER_EYE to Potions.WEAKNESS
	)
	
	val REVERSAL = mapOf(
		Potions.INSTANT_HEALTH to Potions.INSTANT_DAMAGE,
		Potions.POISON         to Potions.INSTANT_DAMAGE,
		Potions.SPEED          to Potions.SLOWNESS,
		Potions.JUMP_BOOST     to Potions.SLOWNESS,
		Potions.NIGHT_VISION   to Potions.INVISIBILITY,
		// TODO CORRUPTION     to BANISHMENT,
		Potions.BLINDNESS      to Potions.GLOWING
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
