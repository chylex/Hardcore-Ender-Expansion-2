package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.recipe.NullRecipe
import chylex.hee.game.recipe.RecipeBindingEssence
import chylex.hee.game.recipe.RecipeEndPowderRepair
import chylex.hee.game.recipe.RecipeJarODustExtract
import chylex.hee.game.recipe.RecipePortalTokenDuplication
import chylex.hee.game.recipe.RecipeScaleOfFreefallRepair
import chylex.hee.game.recipe.RecipeVoidSalad
import chylex.hee.game.recipe.ShapedRecipeWithEnergy
import chylex.hee.game.recipe.factories.IngredientFullEnergy
import chylex.hee.game.recipe.factories.IngredientNoInfusions
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.named
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModRecipes{
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<IRecipeSerializer<*>>){
		CraftingHelper.register(Resource.Custom("ingredient_full_energy"), IngredientFullEnergy)
		CraftingHelper.register(Resource.Custom("ingredient_no_infusions"), IngredientNoInfusions)
		
		with(e.registry){
			// TODO figure out what to do with the broken advancements
			
			register(NullRecipe.serializer named "null")
			register(RecipeBindingEssence.serializer named "dynamic_binding_essence")
			register(RecipeEndPowderRepair.serializer named "dynamic_end_powder_repair")
			register(RecipeJarODustExtract.serializer named "dynamic_jar_o_dust_extract")
			register(RecipePortalTokenDuplication.serializer named "dynamic_portal_token_duplication")
			register(RecipeScaleOfFreefallRepair.serializer named "dynamic_scale_of_freefall_repair")
			register(RecipeVoidSalad.serializer named "dynamic_void_salad")
			register(ShapedRecipeWithEnergy named "crafting_shaped_with_energy")
		}
	}
}
