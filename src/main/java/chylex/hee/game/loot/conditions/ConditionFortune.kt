package chylex.hee.game.loot.conditions
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.loot.BlockLootTable.BlockLootContext
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.util.JsonUtils
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.conditions.LootCondition
import java.util.Random

class ConditionFortune(private val minLevel: Int, private val maxLevel: Int): LootCondition{
	override fun testCondition(rand: Random, context: LootContext): Boolean{
		return context is BlockLootContext && context.fortune in minLevel..maxLevel
	}
	
	object Serializer: LootCondition.Serializer<ConditionFortune>(ResourceLocation(HardcoreEnderExpansion.ID, "fortune"), ConditionFortune::class.java){
		private const val MIN_FORTUNE = 0
		private const val MAX_FORTUNE = Int.MAX_VALUE
		
		override fun serialize(json: JsonObject, value: ConditionFortune, context: JsonSerializationContext){
			if (value.minLevel == value.maxLevel){
				json.addProperty("level", value.minLevel)
			}
			else{
				json.add("level", JsonObject().apply {
					if (value.minLevel != MIN_FORTUNE){
						addProperty("min", value.minLevel)
					}
					
					if (value.maxLevel != MAX_FORTUNE){
						addProperty("max", value.maxLevel)
					}
				})
			}
		}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext): ConditionFortune{
			val minLevel: Int
			val maxLevel: Int
			
			if (JsonUtils.isNumber(json["level"])){
				minLevel = JsonUtils.getInt(json, "level")
				maxLevel = minLevel
			}
			else{
				val range = JsonUtils.getJsonObject(json, "level")
				minLevel = JsonUtils.getInt(range, "min", MIN_FORTUNE)
				maxLevel = JsonUtils.getInt(range, "max", MAX_FORTUNE)
			}
			
			return ConditionFortune(minLevel, maxLevel)
		}
	}
}
