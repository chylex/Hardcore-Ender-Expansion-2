package chylex.hee.game.loot.conditions

import chylex.hee.init.ModLoot
import net.minecraft.loot.LootConditionType
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameters

class ConditionWasExploded(override val expectedValue: Boolean) : ILootConditionBoolean {
	override fun isTrue(context: LootContext): Boolean {
		return context.has(LootParameters.EXPLOSION_RADIUS)
	}
	
	override fun getConditionType(): LootConditionType {
		return ModLoot.CONDITION_WAS_EXPLODED
	}
	
	object Serializer : ILootConditionBoolean.Serializer<ConditionWasExploded>() {
		override fun construct(expectedValue: Boolean) = ConditionWasExploded(expectedValue)
	}
}
