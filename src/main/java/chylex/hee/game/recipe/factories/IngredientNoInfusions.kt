package chylex.hee.game.recipe.factories
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.recipe.factories.IngredientNoInfusions.Instance
import chylex.hee.system.util.getIfExists
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraft.network.PacketBuffer
import net.minecraft.util.JSONUtils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.IIngredientSerializer
import net.minecraftforge.registries.ForgeRegistries
import java.util.stream.Stream

object IngredientNoInfusions : IIngredientSerializer<Instance>{
	override fun write(buffer: PacketBuffer, ingredient: Instance){
		buffer.writeRegistryId(ingredient.item as Item)
	}
	
	override fun parse(buffer: PacketBuffer): Instance{
		return construct(buffer.readRegistryIdSafe(Item::class.java))
	}
	
	override fun parse(json: JsonObject): Instance{
		val itemName = JSONUtils.getString(json, "item")
		val item = ForgeRegistries.ITEMS.getIfExists(ResourceLocation(itemName))
		
		if (item == null){
			throw JsonSyntaxException("Unknown item '$itemName'")
		}
		
		return construct(item)
	}
	
	private fun construct(item: Item): Instance{
		if (item !is IInfusableItem){
			throw JsonSyntaxException("Item '${item.registryName}' is not infusable")
		}
		
		return Instance(ItemStack(item), item)
	}
	
	class Instance(stack: ItemStack, val item: IInfusableItem) : Ingredient(Stream.of(SingleItemList(stack))){
		override fun test(ingredient: ItemStack?): Boolean{
			return ingredient != null && ingredient.item === item && !InfusionTag.hasAny(ingredient)
		}
	}
}
