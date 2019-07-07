package chylex.hee.game.loot.functions
import chylex.hee.system.Resource
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.collection.WeightedList.Companion.weightedListOf
import chylex.hee.system.util.size
import com.google.common.collect.ImmutableBiMap
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.functions.LootFunction
import java.util.Random

class FunctionPickUndreadGem(conditions: Array<LootCondition>, private val items: WeightedList<Item>) : LootFunction(conditions){
	private companion object{
		private val NUGGETS = weightedListOf(
			10 to Items.IRON_NUGGET,
			 7 to Items.GOLD_NUGGET
		)
		
		private val INGOTS = weightedListOf(
			10 to Items.IRON_INGOT,
			 7 to Items.GOLD_INGOT
		)
		
		// TODO add modded nuggets/ingots
		
		private val NAMES = ImmutableBiMap.copyOf(mapOf(
			"nugget" to NUGGETS,
			"ingot" to INGOTS
		))
	}
	
	override fun apply(stack: ItemStack, rand: Random, context: LootContext): ItemStack{
		return ItemStack(items.generateItem(rand), stack.size, stack.metadata)
	}
	
	object Serializer : LootFunction.Serializer<FunctionPickUndreadGem>(Resource.Custom("pick_undread_gem"), FunctionPickUndreadGem::class.java){
		override fun serialize(json: JsonObject, value: FunctionPickUndreadGem, context: JsonSerializationContext){
			json.addProperty("type", NAMES.inverse().getValue(value.items))
		}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext, conditions: Array<LootCondition>): FunctionPickUndreadGem{
			return FunctionPickUndreadGem(conditions, NAMES.getValue(json.getAsJsonPrimitive("type").asString))
		}
	}
}
