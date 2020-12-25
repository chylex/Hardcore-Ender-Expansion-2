package chylex.hee.game.loot.conditions

import chylex.hee.system.facades.Resource
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameters

class ConditionWasExploded(override val expectedValue: Boolean) : ILootConditionBoolean {
	override fun isTrue(context: LootContext): Boolean {
		return context.has(LootParameters.EXPLOSION_RADIUS)
	}
	
	object Serializer : ILootConditionBoolean.Serializer<ConditionWasExploded>(Resource.Custom("was_exploded"), ConditionWasExploded::class.java) {
		override fun construct(expectedValue: Boolean) = ConditionWasExploded(expectedValue)
	}
}
