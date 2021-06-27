package chylex.hee.game.loot.conditions

import chylex.hee.init.ModLoot
import net.minecraft.loot.LootConditionType
import net.minecraft.loot.LootContext

class ConditionLooting(override val minLevel: Int, override val maxLevel: Int) : ILootConditionWithRange {
	override fun test(context: LootContext): Boolean {
		return context.lootingModifier in minLevel..maxLevel
	}
	
	override fun getConditionType(): LootConditionType {
		return ModLoot.CONDITION_LOOTING
	}
	
	object Serializer : ILootConditionWithRange.Serializer<ConditionLooting>() {
		override fun construct(minLevel: Int, maxLevel: Int) = ConditionLooting(minLevel, maxLevel)
	}
}
