package chylex.hee.game.loot.conditions

import chylex.hee.game.world.Pos
import chylex.hee.game.world.getBlock
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModLoot
import net.minecraft.loot.LootConditionType
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameter
import net.minecraft.loot.LootParameters

class ConditionPlantedOnHumus(override val expectedValue: Boolean) : ILootConditionBoolean {
	override fun isTrue(context: LootContext): Boolean {
		val origin = context.get(LootParameters.ORIGIN)
		val isHumus = origin != null && Pos(origin).down().getBlock(context.world) === ModBlocks.HUMUS
		return isHumus == expectedValue
	}
	
	override fun getRequiredParameters(): MutableSet<LootParameter<*>> {
		return mutableSetOf(LootParameters.ORIGIN)
	}
	
	override fun getConditionType(): LootConditionType {
		return ModLoot.CONDITION_PLANTED_ON_HUMUS
	}
	
	object Serializer : ILootConditionBoolean.Serializer<ConditionPlantedOnHumus>() {
		override fun construct(expectedValue: Boolean): ConditionPlantedOnHumus {
			return ConditionPlantedOnHumus(expectedValue)
		}
	}
}
