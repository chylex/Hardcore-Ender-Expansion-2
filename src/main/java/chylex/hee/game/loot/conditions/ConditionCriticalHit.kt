package chylex.hee.game.loot.conditions
import chylex.hee.game.entity.util.ICritTracker
import chylex.hee.system.util.facades.Resource
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.util.JSONUtils
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameter
import net.minecraft.world.storage.loot.LootParameters
import net.minecraft.world.storage.loot.conditions.ILootCondition

class ConditionCriticalHit(private val expectedValue: Boolean) : ILootCondition{
	override fun test(context: LootContext): Boolean{
		val wasCrit = context.get(LootParameters.LAST_DAMAGE_PLAYER) != null && (context.get(LootParameters.THIS_ENTITY) as? ICritTracker)?.wasLastHitCritical == true
		return wasCrit == expectedValue
	}
	
	override fun getRequiredParameters(): MutableSet<LootParameter<*>>{
		return mutableSetOf(LootParameters.LAST_DAMAGE_PLAYER, LootParameters.THIS_ENTITY)
	}
	
	object Serializer : ILootCondition.AbstractSerializer<ConditionCriticalHit>(Resource.Custom("killed_by_critical_hit"), ConditionCriticalHit::class.java){
		override fun serialize(json: JsonObject, value: ConditionCriticalHit, context: JsonSerializationContext){
			json.addProperty("value", value.expectedValue)
		}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext): ConditionCriticalHit{
			return ConditionCriticalHit(JSONUtils.getBoolean(json, "value", true))
		}
	}
}
