package chylex.hee.game.loot.conditions

import chylex.hee.init.ModLoot
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.loot.LootConditionType
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameter
import net.minecraft.loot.LootParameters

class ConditionSilkTouch(override val expectedValue: Boolean) : ILootConditionBoolean {
	override fun isTrue(context: LootContext): Boolean {
		return (context.get(LootParameters.TOOL)?.let { EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, it) } ?: 0) > 0
	}
	
	override fun getRequiredParameters(): MutableSet<LootParameter<*>> {
		return mutableSetOf(LootParameters.TOOL)
	}
	
	override fun getConditionType(): LootConditionType {
		return ModLoot.CONDITION_SILK_TOUCH
	}
	
	object Serializer : ILootConditionBoolean.Serializer<ConditionSilkTouch>() {
		override fun construct(expectedValue: Boolean) = ConditionSilkTouch(expectedValue)
	}
}
