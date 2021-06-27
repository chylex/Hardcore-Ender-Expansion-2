package chylex.hee.game.loot.conditions

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.loot.ILootSerializer
import net.minecraft.loot.conditions.ILootCondition
import net.minecraft.util.JSONUtils

interface ILootConditionWithRange : ILootCondition {
	val minLevel: Int
	val maxLevel: Int
	
	abstract class Serializer<T : ILootConditionWithRange> : ILootSerializer<T> {
		private companion object {
			private const val MIN_LEVEL = 0
			private const val MAX_LEVEL = Int.MAX_VALUE
		}
		
		protected abstract fun construct(minLevel: Int, maxLevel: Int): T
		
		final override fun serialize(json: JsonObject, value: T, context: JsonSerializationContext) {
			if (value.minLevel == value.maxLevel) {
				json.addProperty("level", value.minLevel)
			}
			else {
				json.add("level", JsonObject().apply {
					if (value.minLevel != MIN_LEVEL) {
						addProperty("min", value.minLevel)
					}
					
					if (value.maxLevel != MAX_LEVEL) {
						addProperty("max", value.maxLevel)
					}
				})
			}
		}
		
		final override fun deserialize(json: JsonObject, context: JsonDeserializationContext): T {
			val minLevel: Int
			val maxLevel: Int
			
			if (JSONUtils.isNumber(json["level"])) {
				minLevel = JSONUtils.getInt(json, "level")
				maxLevel = minLevel
			}
			else {
				val range = JSONUtils.getJsonObject(json, "level")
				minLevel = JSONUtils.getInt(range, "min", MIN_LEVEL)
				maxLevel = JSONUtils.getInt(range, "max", MAX_LEVEL)
			}
			
			return construct(minLevel, maxLevel)
		}
	}
}
