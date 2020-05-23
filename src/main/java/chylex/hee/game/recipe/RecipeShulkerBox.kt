package chylex.hee.game.recipe
import chylex.hee.game.block.BlockShulkerBoxOverride.BoxSize
import chylex.hee.game.item.ItemShulkerBoxOverride
import com.google.gson.JsonObject
import net.minecraft.item.crafting.ShapedRecipe
import net.minecraft.util.ResourceLocation

object RecipeShulkerBox : ShapedRecipe.Serializer(){
	override fun read(location: ResourceLocation, json: JsonObject): ShapedRecipe{
		return super.read(location, json).also { ItemShulkerBoxOverride.setBoxSize(it.recipeOutput, BoxSize.SMALL) }
	}
}
