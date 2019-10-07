package chylex.hee.game.loot.conditions
import chylex.hee.game.entity.util.ICritTracker
import chylex.hee.system.util.facades.Resource
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.util.JsonUtils
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.conditions.LootCondition
import java.util.Random

class ConditionCriticalHit(private val expectedValue: Boolean) : LootCondition{
	override fun testCondition(rand: Random, context: LootContext): Boolean{
		val wasCrit = context.killerPlayer != null && (context.lootedEntity as? ICritTracker)?.wasLastHitCritical == true // killerPlayer != null only if recentlyHit > 0
		return wasCrit == expectedValue
	}
	
	object Serializer : LootCondition.Serializer<ConditionCriticalHit>(Resource.Custom("killed_by_critical_hit"), ConditionCriticalHit::class.java){
		override fun serialize(json: JsonObject, value: ConditionCriticalHit, context: JsonSerializationContext){
			json.addProperty("value", value.expectedValue)
		}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext): ConditionCriticalHit{
			return ConditionCriticalHit(JsonUtils.getBoolean(json, "value", true))
		}
	}
}
