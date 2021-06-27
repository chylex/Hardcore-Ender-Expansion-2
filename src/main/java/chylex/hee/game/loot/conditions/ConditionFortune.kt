package chylex.hee.game.loot.conditions

import chylex.hee.init.ModLoot
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.loot.LootConditionType
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameter
import net.minecraft.loot.LootParameters

class ConditionFortune(override val minLevel: Int, override val maxLevel: Int) : ILootConditionWithRange {
	override fun test(context: LootContext): Boolean {
		return (context.get(LootParameters.TOOL)?.let { EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, it) } ?: 0) in minLevel..maxLevel
	}
	
	override fun getRequiredParameters(): MutableSet<LootParameter<*>> {
		return mutableSetOf(LootParameters.TOOL)
	}
	
	override fun getConditionType(): LootConditionType {
		return ModLoot.CONDITION_FORTUNE
	}
	
	object Serializer : ILootConditionWithRange.Serializer<ConditionFortune>() {
		override fun construct(minLevel: Int, maxLevel: Int) = ConditionFortune(minLevel, maxLevel)
	}
}
