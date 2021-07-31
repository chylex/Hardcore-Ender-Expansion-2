package chylex.hee.game.potion.brewing

import chylex.hee.game.potion.brewing.PotionTypeInfo.Duration
import chylex.hee.init.ModEffects
import chylex.hee.init.ModItems
import chylex.hee.util.math.floorToInt
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.potion.Effects
import net.minecraft.potion.PotionUtils

object PotionBrewing {
	const val INFINITE_DURATION = 32767
	const val INFINITE_DURATION_THRESHOLD = 32147 // values >= this threshold should be considered infinite
	
	val INFO = arrayOf(
		PotionTypeInfo(Effects.INSTANT_HEALTH,  maxLevel = 2),
		PotionTypeInfo(Effects.FIRE_RESISTANCE, Duration(baseTicks = 3 min 20, stepTicks = 3 min 10,    maxSteps = 4), maxLevel = 1),
		PotionTypeInfo(Effects.REGENERATION,    Duration(baseTicks = 0 min 30, stepTicks = 0 min 30,    maxSteps = 3), maxLevel = 3),
		PotionTypeInfo(Effects.STRENGTH,        Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(Effects.SPEED,           Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(Effects.NIGHT_VISION,    Duration(baseTicks = 3 min 20, stepTicks = 3 min 10,    maxSteps = 4), maxLevel = 1),
		PotionTypeInfo(Effects.WATER_BREATHING, Duration(baseTicks = 3 min 20, stepTicks = 3 min 10,    maxSteps = 4), maxLevel = 1),
		PotionTypeInfo(Effects.JUMP_BOOST,      Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(Effects.POISON,          Duration(baseTicks = 0 min 30, stepTicks = 0 min 30,    maxSteps = 3), maxLevel = 2),
		PotionTypeInfo(Effects.SLOW_FALLING,    Duration(baseTicks = 1 min 15, stepTicks = 1 min 15,    maxSteps = 3), maxLevel = 1),
		PotionTypeInfo(Effects.LEVITATION,      Duration(baseTicks = 0 min 30, stepTicks = 0 min 30,    maxSteps = 3), maxLevel = 2),
		PotionTypeInfo(ModEffects.PURITY,       Duration(baseTicks = 2 min 30, stepTicks = 2 min 15,    maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(ModEffects.CORRUPTION,   Duration(baseTicks = 0 min 20, stepTicks = 0 min 13.34, maxSteps = 3), maxLevel = 1),
		PotionTypeInfo(Effects.BLINDNESS,       Duration(baseTicks = 0 min 20, stepTicks = 0 min 13.34, maxSteps = 3), maxLevel = 1),
		PotionTypeInfo(Effects.WEAKNESS,        Duration(baseTicks = 1 min 15, stepTicks = 1 min 15,    maxSteps = 3), maxLevel = 3),
		
		PotionTypeInfo(Effects.INSTANT_DAMAGE,  maxLevel = 2),
		PotionTypeInfo(Effects.SLOWNESS,        Duration(baseTicks = 1 min 15, stepTicks = 1 min 7.5, maxSteps = 4), maxLevel = 3),
		PotionTypeInfo(Effects.INVISIBILITY,    Duration(baseTicks = 1 min 20, stepTicks = 1 min 16,  maxSteps = 4), maxLevel = 1),
		PotionTypeInfo(ModEffects.BANISHMENT,   Duration(baseTicks = 0 min 15, stepTicks = 0 min 10,  maxSteps = 3), maxLevel = 1),
		PotionTypeInfo(Effects.GLOWING,         Duration(baseTicks = 0 min 12, stepTicks = 0 min 8,   maxSteps = 3), maxLevel = 1)
	).associateBy(PotionTypeInfo::effect)
	
	// TODO register levitation type and other custom potions
	
	// UPDATE 1.16 check new potions
	
	val AWKWARD = mapOf(
		Items.GLISTERING_MELON_SLICE to Effects.INSTANT_HEALTH,
		Items.MAGMA_CREAM            to Effects.FIRE_RESISTANCE,
		Items.GHAST_TEAR             to Effects.REGENERATION,
		Items.BLAZE_POWDER           to Effects.STRENGTH,
		Items.SUGAR                  to Effects.SPEED,
		Items.GOLDEN_CARROT          to Effects.NIGHT_VISION,
		Items.PUFFERFISH             to Effects.WATER_BREATHING,
		Items.RABBIT_FOOT            to Effects.JUMP_BOOST,
		Items.SPIDER_EYE             to Effects.POISON,
		Items.PHANTOM_MEMBRANE       to Effects.SLOW_FALLING,
		ModItems.DRAGON_SCALE        to Effects.LEVITATION,
		ModItems.PURITY_EXTRACT      to ModEffects.PURITY,
		ModItems.INSTABILITY_ORB     to ModEffects.CORRUPTION
		// TODO ModItems.MURKY_CRYSTAL       to Effects.BLINDNESS
	)
	
	val WATER = mapOf(
		Items.FERMENTED_SPIDER_EYE to Effects.WEAKNESS
	)
	
	val REVERSAL = mapOf(
		Effects.INSTANT_HEALTH to Effects.INSTANT_DAMAGE,
		Effects.POISON         to Effects.INSTANT_DAMAGE,
		Effects.SPEED          to Effects.SLOWNESS,
		Effects.JUMP_BOOST     to Effects.SLOWNESS,
		Effects.NIGHT_VISION   to Effects.INVISIBILITY,
		ModEffects.CORRUPTION  to ModEffects.BANISHMENT,
		Effects.BLINDNESS      to Effects.GLOWING
	)
	
	fun unpack(stack: ItemStack): PotionTypeInfo.Instance? {
		val effects = PotionUtils.getEffectsFromStack(stack)
		
		if (effects.size != 1) {
			return null
		}
		
		val effect = effects[0]
		val potion = effect.potion
		
		return INFO[potion]?.Instance(stack, effect)
	}
	
	fun isAltered(stack: ItemStack): Boolean {
		return unpack(stack) != null || (PotionItems.isPotion(stack) && PotionUtils.getEffectsFromStack(stack).isEmpty())
	}
	
	// Utilities
	
	private infix fun Int.min(seconds: Int): Int {
		return ((this * 60) + seconds) * 20
	}
	
	private infix fun Int.min(seconds: Double): Int {
		return (((this * 60) + seconds) * 20).floorToInt()
	}
}
