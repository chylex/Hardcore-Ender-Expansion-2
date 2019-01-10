package chylex.hee.game.loot.conditions
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.util.JsonUtils
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.conditions.LootCondition

interface ILootConditionWithRange : LootCondition{
	val minLevel: Int
	val maxLevel: Int
	
	abstract class Serializer<T : ILootConditionWithRange>(key: ResourceLocation, cls: Class<T>) : LootCondition.Serializer<T>(key, cls){
		private companion object{
			private const val MIN_LEVEL = 0
			private const val MAX_LEVEL = Int.MAX_VALUE
		}
		
		protected abstract fun construct(minLevel: Int, maxLevel: Int): T
		
		final override fun serialize(json: JsonObject, value: T, context: JsonSerializationContext){
			if (value.minLevel == value.maxLevel){
				json.addProperty("level", value.minLevel)
			}
			else{
				json.add("level", JsonObject().apply {
					if (value.minLevel != MIN_LEVEL){
						addProperty("min", value.minLevel)
					}
					
					if (value.maxLevel != MAX_LEVEL){
						addProperty("max", value.maxLevel)
					}
				})
			}
		}
		
		final override fun deserialize(json: JsonObject, context: JsonDeserializationContext): T{
			val minLevel: Int
			val maxLevel: Int
			
			if (JsonUtils.isNumber(json["level"])){
				minLevel = JsonUtils.getInt(json, "level")
				maxLevel = minLevel
			}
			else{
				val range = JsonUtils.getJsonObject(json, "level")
				minLevel = JsonUtils.getInt(range, "min", MIN_LEVEL)
				maxLevel = JsonUtils.getInt(range, "max", MAX_LEVEL)
			}
			
			return construct(minLevel, maxLevel)
		}
	}
}
