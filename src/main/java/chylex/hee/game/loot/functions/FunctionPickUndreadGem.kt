package chylex.hee.game.loot.functions

import chylex.hee.game.item.util.size
import chylex.hee.init.ModLoot
import chylex.hee.util.collection.WeightedList
import chylex.hee.util.collection.weightedListOf
import com.google.common.collect.ImmutableBiMap
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootFunction
import net.minecraft.loot.LootFunctionType
import net.minecraft.loot.conditions.ILootCondition

class FunctionPickUndreadGem(conditions: Array<ILootCondition>, private val items: WeightedList<Item>) : LootFunction(conditions) {
	private companion object {
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
	
	override fun doApply(stack: ItemStack, context: LootContext): ItemStack {
		return ItemStack(items.generateItem(context.random), stack.size)
	}
	
	override fun getFunctionType(): LootFunctionType {
		return ModLoot.FUNCTION_PICK_UNDREAD_GEM
	}
	
	object Serializer : LootFunction.Serializer<FunctionPickUndreadGem>() {
		override fun serialize(json: JsonObject, value: FunctionPickUndreadGem, context: JsonSerializationContext) {
			json.addProperty("type", NAMES.inverse().getValue(value.items))
		}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext, conditions: Array<ILootCondition>): FunctionPickUndreadGem {
			return FunctionPickUndreadGem(conditions, NAMES.getValue(json.getAsJsonPrimitive("type").asString))
		}
	}
}
