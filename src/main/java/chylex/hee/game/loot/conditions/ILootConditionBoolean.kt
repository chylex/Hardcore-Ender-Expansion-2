package chylex.hee.game.loot.conditions

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.loot.ILootSerializer
import net.minecraft.loot.LootContext
import net.minecraft.loot.conditions.ILootCondition
import net.minecraft.util.JSONUtils

interface ILootConditionBoolean : ILootCondition {
	val expectedValue: Boolean
	
	fun isTrue(context: LootContext): Boolean
	
	override fun test(context: LootContext): Boolean {
		return isTrue(context) == expectedValue
	}
	
	abstract class Serializer<T : ILootConditionBoolean> : ILootSerializer<T> {
		protected abstract fun construct(expectedValue: Boolean): T
		
		final override fun serialize(json: JsonObject, value: T, context: JsonSerializationContext) {
			json.addProperty("value", value.expectedValue)
		}
		
		final override fun deserialize(json: JsonObject, context: JsonDeserializationContext): T {
			return construct(JSONUtils.getBoolean(json, "value"))
		}
	}
}
