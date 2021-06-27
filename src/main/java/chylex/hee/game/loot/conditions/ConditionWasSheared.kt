package chylex.hee.game.loot.conditions

import chylex.hee.init.ModLoot
import chylex.hee.system.migration.ItemShears
import net.minecraft.loot.LootConditionType
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameter
import net.minecraft.loot.LootParameters

class ConditionWasSheared(override val expectedValue: Boolean) : ILootConditionBoolean {
	override fun isTrue(context: LootContext): Boolean {
		return context.get(LootParameters.TOOL)?.let { it.item is ItemShears } == true
	}
	
	override fun getRequiredParameters(): MutableSet<LootParameter<*>> {
		return mutableSetOf(LootParameters.TOOL)
	}
	
	override fun getConditionType(): LootConditionType {
		return ModLoot.CONDITION_WAS_SHEARED
	}
	
	object Serializer : ILootConditionBoolean.Serializer<ConditionWasSheared>() {
		override fun construct(expectedValue: Boolean) = ConditionWasSheared(expectedValue)
	}
}
