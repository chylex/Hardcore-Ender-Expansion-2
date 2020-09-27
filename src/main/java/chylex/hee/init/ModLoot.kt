package chylex.hee.init
import chylex.hee.game.loot.conditions.ConditionCriticalHit
import chylex.hee.game.loot.conditions.ConditionFortune
import chylex.hee.game.loot.conditions.ConditionLooting
import chylex.hee.game.loot.conditions.ConditionSilkTouch
import chylex.hee.game.loot.conditions.ConditionWasExploded
import chylex.hee.game.loot.conditions.ConditionWasSheared
import chylex.hee.game.loot.functions.FunctionInfuse
import chylex.hee.game.loot.functions.FunctionPickUndreadGem
import chylex.hee.game.loot.functions.FunctionSetItem
import net.minecraft.world.storage.loot.conditions.LootConditionManager
import net.minecraft.world.storage.loot.functions.LootFunctionManager

object ModLoot{
	fun initialize(){
		LootConditionManager.registerCondition(ConditionCriticalHit.Serializer)
		LootConditionManager.registerCondition(ConditionFortune.Serializer)
		LootConditionManager.registerCondition(ConditionLooting.Serializer)
		LootConditionManager.registerCondition(ConditionSilkTouch.Serializer)
		LootConditionManager.registerCondition(ConditionWasExploded.Serializer)
		LootConditionManager.registerCondition(ConditionWasSheared.Serializer)
		
		LootFunctionManager.registerFunction(FunctionInfuse.Serializer)
		LootFunctionManager.registerFunction(FunctionPickUndreadGem.Serializer)
		LootFunctionManager.registerFunction(FunctionSetItem.Serializer)
	}
}
