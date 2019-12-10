package chylex.hee.game.recipe.factories
import chylex.hee.game.item.ItemAbstractEnergyUser
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
class IngredientFullEnergy : IIngredientFactory{
	override fun parse(context: JsonContext, json: JsonObject): Ingredient{
		val itemName = JsonUtils.getString(json, "item")
		val item = ForgeRegistries.ITEMS.getIfExists(ResourceLocation(itemName))
		
		if (item == null){
			throw JsonSyntaxException("Unknown item '$itemName'")
		}
		
		if (item.hasSubtypes){
			throw JsonSyntaxException("Item '$itemName' must not have subtypes")
		}
		
		if (item !is ItemAbstractEnergyUser){
			throw JsonSyntaxException("Item '$itemName' does not use Energy")
		}
		
		val stack = ItemStack(item).also {
			item.setEnergyChargePercentage(it, 1F)
		}
		
		return Instance(stack, item)
	}
	
	private class Instance(stack: ItemStack, private val item: ItemAbstractEnergyUser) : Ingredient(stack){
		override fun apply(ingredient: ItemStack?): Boolean{
			return ingredient != null && ingredient.item === item && item.hasMaximumEnergy(ingredient) && !InfusionTag.hasAny(ingredient)
		}
	}
}
