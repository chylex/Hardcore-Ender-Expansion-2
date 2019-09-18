package chylex.hee.game.loot.functions
import chylex.hee.init.ModBlocks
import chylex.hee.system.Resource
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.size
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.item.ItemStack
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.functions.LootFunction
import java.util.Random

class FunctionPickColoredGloomrock(conditions: Array<LootCondition>) : LootFunction(conditions){
	private companion object{
		private val BLOCKS = arrayOf(
			ModBlocks.GLOOMROCK_SMOOTH_RED,
			ModBlocks.GLOOMROCK_SMOOTH_ORANGE,
			ModBlocks.GLOOMROCK_SMOOTH_YELLOW,
			ModBlocks.GLOOMROCK_SMOOTH_GREEN,
			ModBlocks.GLOOMROCK_SMOOTH_CYAN,
			ModBlocks.GLOOMROCK_SMOOTH_BLUE,
			ModBlocks.GLOOMROCK_SMOOTH_PURPLE,
			ModBlocks.GLOOMROCK_SMOOTH_MAGENTA,
			ModBlocks.GLOOMROCK_SMOOTH_WHITE
		)
	}
	
	override fun apply(stack: ItemStack, rand: Random, context: LootContext): ItemStack{
		return ItemStack(rand.nextItem(BLOCKS), stack.size, stack.metadata)
	}
	
	object Serializer : LootFunction.Serializer<FunctionPickColoredGloomrock>(Resource.Custom("pick_colored_gloomrock"), FunctionPickColoredGloomrock::class.java){
		override fun serialize(json: JsonObject, value: FunctionPickColoredGloomrock, context: JsonSerializationContext){}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext, conditions: Array<LootCondition>): FunctionPickColoredGloomrock{
			return FunctionPickColoredGloomrock(conditions)
		}
	}
}
