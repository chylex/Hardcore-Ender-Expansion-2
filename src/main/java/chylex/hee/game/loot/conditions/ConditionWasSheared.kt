package chylex.hee.game.loot.conditions
import chylex.hee.system.facades.Resource
import chylex.hee.system.migration.ItemShears
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameter
import net.minecraft.world.storage.loot.LootParameters

class ConditionWasSheared(override val expectedValue: Boolean): ILootConditionBoolean{
	override fun isTrue(context: LootContext): Boolean{
		return context.get(LootParameters.TOOL)?.let { it.item is ItemShears } == true
	}
	
	override fun getRequiredParameters(): MutableSet<LootParameter<*>>{
		return mutableSetOf(LootParameters.TOOL)
	}
	
	object Serializer : ILootConditionBoolean.Serializer<ConditionWasSheared>(Resource.Custom("was_sheared"), ConditionWasSheared::class.java){
		override fun construct(expectedValue: Boolean) = ConditionWasSheared(expectedValue)
	}
}
