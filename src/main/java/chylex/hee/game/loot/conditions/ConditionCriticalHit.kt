package chylex.hee.game.loot.conditions

import chylex.hee.game.entity.living.ICritTracker
import chylex.hee.init.ModLoot
import net.minecraft.loot.LootConditionType
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameter
import net.minecraft.loot.LootParameters

class ConditionCriticalHit(override val expectedValue: Boolean) : ILootConditionBoolean {
	override fun isTrue(context: LootContext): Boolean {
		return context.get(LootParameters.LAST_DAMAGE_PLAYER) != null && (context.get(LootParameters.THIS_ENTITY) as? ICritTracker)?.wasLastHitCritical == true
	}
	
	override fun getRequiredParameters(): MutableSet<LootParameter<*>> {
		return mutableSetOf(LootParameters.LAST_DAMAGE_PLAYER, LootParameters.THIS_ENTITY)
	}
	
	override fun getConditionType(): LootConditionType {
		return ModLoot.CONDITION_CRITICAL_HIT
	}
	
	object Serializer : ILootConditionBoolean.Serializer<ConditionCriticalHit>() {
		override fun construct(expectedValue: Boolean) = ConditionCriticalHit(expectedValue)
	}
}
