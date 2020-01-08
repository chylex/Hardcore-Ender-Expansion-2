package chylex.hee.game.loot.functions
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.size
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.JSONUtils
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootFunction
import net.minecraft.world.storage.loot.conditions.ILootCondition

class FunctionPickMusicDisk(conditions: Array<ILootCondition>, private val picks: Array<Item>) : LootFunction(conditions){
	private companion object{
		private val DISKS = mapOf(
			"obsidiantower" to arrayOf(
				Items.MUSIC_DISC_13,
				Items.MUSIC_DISC_CAT,
				Items.MUSIC_DISC_BLOCKS,
				Items.MUSIC_DISC_CHIRP,
				Items.MUSIC_DISC_FAR,
				Items.MUSIC_DISC_MALL,
				Items.MUSIC_DISC_MELLOHI,
				Items.MUSIC_DISC_STAL,
				Items.MUSIC_DISC_STRAD
				// TODO replace with custom music disks
			)
		)
	}
	
	override fun doApply(stack: ItemStack, context: LootContext): ItemStack{
		return ItemStack(context.random.nextItem(picks), stack.size)
	}
	
	object Serializer : LootFunction.Serializer<FunctionPickMusicDisk>(Resource.Custom("pick_music_disk"), FunctionPickMusicDisk::class.java){
		override fun serialize(json: JsonObject, value: FunctionPickMusicDisk, context: JsonSerializationContext){}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext, conditions: Array<ILootCondition>): FunctionPickMusicDisk{
			return FunctionPickMusicDisk(conditions, DISKS.getValue(JSONUtils.getString(json, "type")))
		}
	}
}
