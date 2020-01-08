package chylex.hee.game.loot.functions
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.size
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import net.minecraft.item.ItemStack
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootFunction
import net.minecraft.world.storage.loot.conditions.ILootCondition

class FunctionPickColoredGloomrock(conditions: Array<ILootCondition>) : LootFunction(conditions){
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
	
	override fun doApply(stack: ItemStack, context: LootContext): ItemStack{
		return ItemStack(context.random.nextItem(BLOCKS), stack.size)
	}
	
	object Serializer : LootFunction.Serializer<FunctionPickColoredGloomrock>(Resource.Custom("pick_colored_gloomrock"), FunctionPickColoredGloomrock::class.java){
		override fun serialize(json: JsonObject, value: FunctionPickColoredGloomrock, context: JsonSerializationContext){}
		
		override fun deserialize(json: JsonObject, context: JsonDeserializationContext, conditions: Array<ILootCondition>): FunctionPickColoredGloomrock{
			return FunctionPickColoredGloomrock(conditions)
		}
	}
}
