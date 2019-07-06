package chylex.hee.game.loot.functions
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.system.Resource
import chylex.hee.system.util.removeItemOrNull
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.item.ItemStack
import net.minecraft.util.JsonUtils
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.RandomValueRange
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.functions.LootFunction
import java.util.Random

class FunctionInfuse(conditions: Array<LootCondition>, private val picks: Array<Infusion>, private val amount: RandomValueRange) : LootFunction(conditions){
	override fun apply(stack: ItemStack, rand: Random, context: LootContext): ItemStack{
		val availableInfusions = picks.toMutableList()
		var finalStack = stack
		
		for(count in 1..(amount.generateInt(rand))){
			finalStack = rand.removeItemOrNull(availableInfusions)?.tryInfuse(finalStack) ?: break
		}
		
		return finalStack
	}
	
	object Serializer : LootFunction.Serializer<FunctionInfuse>(Resource.Custom("infuse"), FunctionInfuse::class.java){
		override fun serialize(json: JsonObject, value: FunctionInfuse, context: JsonSerializationContext){
			json.add("picks", JsonArray().apply { value.picks.forEach { add(it.name) } })
			json.add("amount", context.serialize(value.amount))
		}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext, conditions: Array<LootCondition>): FunctionInfuse{
			val picks = JsonUtils.getJsonArray(json, "picks").map { Infusion.byName(it.asString) }.toTypedArray()
			val amount = JsonUtils.deserializeClass(json, "amount", context, RandomValueRange::class.java)
			
			return FunctionInfuse(conditions, picks, amount)
		}
	}
}
