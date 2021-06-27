package chylex.hee.game.loot.functions

import chylex.hee.game.item.infusion.Infusion
import chylex.hee.init.ModLoot
import chylex.hee.system.random.removeItemOrNull
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootFunction
import net.minecraft.loot.LootFunctionType
import net.minecraft.loot.RandomValueRange
import net.minecraft.loot.conditions.ILootCondition
import net.minecraft.util.JSONUtils

class FunctionInfuse(conditions: Array<ILootCondition>, private val picks: Array<Infusion>, private val amount: RandomValueRange) : LootFunction(conditions) {
	override fun doApply(stack: ItemStack, context: LootContext): ItemStack {
		val availableInfusions = picks.toMutableList()
		var finalStack = stack
		
		for(count in 1..(amount.generateInt(context.random))) {
			finalStack = context.random.removeItemOrNull(availableInfusions)?.tryInfuse(finalStack) ?: break
		}
		
		return finalStack
	}
	
	override fun getFunctionType(): LootFunctionType {
		return ModLoot.FUNCTION_INFUSE
	}
	
	object Serializer : LootFunction.Serializer<FunctionInfuse>() {
		override fun serialize(json: JsonObject, value: FunctionInfuse, context: JsonSerializationContext) {
			json.add("picks", JsonArray().apply { value.picks.forEach { add(it.name) } })
			json.add("amount", context.serialize(value.amount))
		}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext, conditions: Array<ILootCondition>): FunctionInfuse {
			val picks = JSONUtils.getJsonArray(json, "picks").map { Infusion.byName(it.asString) }.toTypedArray()
			val amount = JSONUtils.deserializeClass(json, "amount", context, RandomValueRange::class.java)
			
			return FunctionInfuse(conditions, picks, amount)
		}
	}
}
