package chylex.hee.game.loot.functions
import chylex.hee.system.Resource
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.size
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.JsonUtils
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.functions.LootFunction
import java.util.Random

class FunctionPickMusicDisk(conditions: Array<LootCondition>, private val picks: Array<Item>) : LootFunction(conditions){
	private companion object{
		private val DISKS = mapOf(
			"obsidiantower" to arrayOf(
				Items.RECORD_13,
				Items.RECORD_CAT,
				Items.RECORD_BLOCKS,
				Items.RECORD_CHIRP,
				Items.RECORD_FAR,
				Items.RECORD_MALL,
				Items.RECORD_MELLOHI,
				Items.RECORD_STAL,
				Items.RECORD_STRAD
				// TODO replace with custom music disks
			)
		)
	}
	
	override fun apply(stack: ItemStack, rand: Random, context: LootContext): ItemStack{
		return ItemStack(rand.nextItem(picks), stack.size, stack.metadata)
	}
	
	object Serializer : LootFunction.Serializer<FunctionPickMusicDisk>(Resource.Custom("pick_music_disk"), FunctionPickMusicDisk::class.java){
		override fun serialize(json: JsonObject, value: FunctionPickMusicDisk, context: JsonSerializationContext){}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext, conditions: Array<LootCondition>): FunctionPickMusicDisk{
			return FunctionPickMusicDisk(conditions, DISKS.getValue(JsonUtils.getString(json, "type")))
		}
	}
}
