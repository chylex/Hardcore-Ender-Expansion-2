package chylex.hee.game.recipe

import chylex.hee.game.item.interfaces.getHeeInterface
import chylex.hee.game.mechanics.energy.IEnergyItem
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.item.crafting.ShapedRecipe
import net.minecraft.util.JSONUtils
import net.minecraft.util.ResourceLocation

object ShapedRecipeWithEnergy : ShapedRecipe.Serializer() {
	override fun read(location: ResourceLocation, json: JsonObject): ShapedRecipe {
		val quantity = JSONUtils.getInt(JSONUtils.getJsonObject(json, "result"), "energy")
		
		return super.read(location, json).also {
			val output = it.recipeOutput
			val item = output.item
			
			val energy = item.getHeeInterface<IEnergyItem>()
			if (energy == null) {
				throw JsonSyntaxException("Recipe result item '${item.registryName}' does not use Energy")
			}
			
			if (quantity == -1) {
				energy.setChargePercentage(output, 1F)
			}
			else {
				energy.setChargeLevel(output, Units(quantity))
			}
		}
	}
}
