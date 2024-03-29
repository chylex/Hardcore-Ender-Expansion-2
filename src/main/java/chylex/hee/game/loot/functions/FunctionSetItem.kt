package chylex.hee.game.loot.functions

import chylex.hee.init.ModLoot
import chylex.hee.system.getIfExists
import chylex.hee.util.random.nextItem
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootFunction
import net.minecraft.loot.LootFunctionType
import net.minecraft.loot.conditions.ILootCondition
import net.minecraft.tags.ITag.INamedTag
import net.minecraft.tags.ItemTags
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

sealed class FunctionSetItem(conditions: Array<ILootCondition>) : LootFunction(conditions) {
	abstract fun serialize(json: JsonObject, context: JsonSerializationContext)
	
	override fun getFunctionType(): LootFunctionType {
		return ModLoot.FUNCTION_SET_ITEM
	}
	
	private class FromList(conditions: Array<ILootCondition>, private val items: Array<String>) : FunctionSetItem(conditions) {
		override fun doApply(stack: ItemStack, context: LootContext): ItemStack {
			return ItemStack.read(stack.serializeNBT().apply {
				putString("id", context.random.nextItem(items))
			})
		}
		
		override fun serialize(json: JsonObject, context: JsonSerializationContext) {
			json.add("items", JsonArray().also { items.forEach(it::add) })
		}
	}
	
	private class FromTag(conditions: Array<ILootCondition>, private val tag: INamedTag<Item>) : FunctionSetItem(conditions) {
		override fun doApply(stack: ItemStack, context: LootContext): ItemStack {
			return ItemStack.read(stack.serializeNBT().apply {
				putString("id", tag.getRandomElement(context.random).registryName.toString())
			})
		}
		
		override fun serialize(json: JsonObject, context: JsonSerializationContext) {
			json.addProperty("tag", tag.name.toString())
		}
	}
	
	object Serializer : LootFunction.Serializer<FunctionSetItem>() {
		override fun serialize(json: JsonObject, value: FunctionSetItem, context: JsonSerializationContext) {
			value.serialize(json, context)
		}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext, conditions: Array<ILootCondition>): FunctionSetItem {
			return when {
				json.has("items") -> {
					val items = json.getAsJsonArray("items").map { it.asString }
					FromList(conditions, items.onEach { ForgeRegistries.ITEMS.getIfExists(ResourceLocation(it)) ?: throw JsonParseException("Can't find item: $it") }.toTypedArray())
				}
				
				json.has("tag") -> {
					val tag = json.get("tag").asString
					val location = ResourceLocation(tag)
					FromTag(conditions, ItemTags.getAllTags().find { it.name == location } ?: throw JsonParseException("Can't find tag: $tag"))
				}
				
				else -> throw JsonParseException("Missing either 'items' or 'tag' tag.")
			}
		}
	}
}
