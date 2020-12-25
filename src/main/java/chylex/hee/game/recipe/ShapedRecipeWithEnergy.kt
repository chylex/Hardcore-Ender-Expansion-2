package chylex.hee.game.recipe

import chylex.hee.game.item.ItemAbstractEnergyUser
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.item.crafting.ShapedRecipe
import net.minecraft.util.JSONUtils
import net.minecraft.util.ResourceLocation

object ShapedRecipeWithEnergy : ShapedRecipe.Serializer() {
	override fun read(location: ResourceLocation, json: JsonObject): ShapedRecipe {
		val energy = JSONUtils.getInt(JSONUtils.getJsonObject(json, "result"), "energy")
		
		return super.read(location, json).also {
			val output = it.recipeOutput
			val item = output.item
			
			if (item !is ItemAbstractEnergyUser) {
				throw JsonSyntaxException("Expected recipe result of type ItemAbstractEnergyUser, was $item")
			}
			
			if (energy == -1) {
				item.setEnergyChargePercentage(output, 1F)
			}
			else {
				item.setEnergyChargeLevel(output, Units(energy))
			}
		}
	}
}
