package chylex.hee.game.recipe.factories
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.system.util.getIfExists
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraft.util.JsonUtils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.IIngredientFactory
import net.minecraftforge.common.crafting.JsonContext
import net.minecraftforge.fml.common.registry.ForgeRegistries

@Suppress("unused")
class IngredientNoInfusions : IIngredientFactory{
	override fun parse(context: JsonContext, json: JsonObject): Ingredient{
		val itemName = JsonUtils.getString(json, "item")
		val item = ForgeRegistries.ITEMS.getIfExists(ResourceLocation(itemName))
		
		if (item == null){
			throw JsonSyntaxException("Unknown item '$itemName'")
		}
		
		if (item.hasSubtypes){
			throw JsonSyntaxException("Item '$itemName' must not have subtypes")
		}
		
		if (item !is IInfusableItem){
			throw JsonSyntaxException("Item '$itemName' is not infusable")
		}
		
		return Instance(ItemStack(item), item)
	}
	
	private class Instance(stack: ItemStack, private val item: IInfusableItem) : Ingredient(stack){
		override fun apply(ingredient: ItemStack?): Boolean{
			return ingredient != null && ingredient.item === item && !InfusionTag.hasAny(ingredient)
		}
	}
}
