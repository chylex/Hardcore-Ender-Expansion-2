package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.recipe.RecipeBindingEssence
import chylex.hee.game.recipe.RecipeEndPowderRepair
import chylex.hee.game.recipe.RecipeJarODustExtract
import chylex.hee.game.recipe.RecipePortalTokenDuplication
import chylex.hee.game.recipe.RecipeScaleOfFreefallRepair
import chylex.hee.game.recipe.RecipeVoidSalad
import chylex.hee.game.recipe.factories.IngredientFullEnergy
import chylex.hee.game.recipe.factories.IngredientNoInfusions
import chylex.hee.system.IntegrityCheck
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
	fun initialize(){ // UPDATE: Move smelting recipes to JSON
		/*with(FurnaceRecipes.instance()){
			addSmeltingRecipeForBlock(ModBlocks.GLOOMROCK, ItemStack(ModBlocks.GLOOMROCK_SMOOTH), 0.1F)
			addSmeltingRecipeForBlock(ModBlocks.DUSTY_STONE_BRICKS, ItemStack(ModBlocks.DUSTY_STONE_CRACKED_BRICKS), 0.1F)
			addSmeltingRecipeForBlock(ModBlocks.DUSTY_STONE, ItemStack(ModBlocks.INFUSED_GLASS), 0.15F)
			addSmeltingRecipeForBlock(ModBlocks.END_POWDER_ORE, ItemStack(ModItems.END_POWDER), 0.15F)
			addSmeltingRecipeForBlock(ModBlocks.ENDIUM_ORE, ItemStack(ModItems.ENDIUM_INGOT), 1.0F)
			addSmeltingRecipeForBlock(ModBlocks.IGNEOUS_ROCK_ORE, ItemStack(ModItems.IGNEOUS_ROCK), 0.2F)
			
			addSmeltingRecipeForBlock(Blocks.VINE, ItemStack(ModBlocks.DRY_VINES), 0.1F)
			
			IntegrityCheck.removedChorusFruitRecipe = smeltingList.remove(smeltingList.keys.find { it.item === Items.CHORUS_FRUIT }) != null
		}*/
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<IRecipeSerializer<*>>){
		CraftingHelper.register(Resource.Custom("ingredient_full_energy"), IngredientFullEnergy)
		CraftingHelper.register(Resource.Custom("ingredient_no_infusions"), IngredientNoInfusions)
		
		with(e.registry){
			fun removeVanilla(name: String): Boolean{
				// UPDATE val removed = remove(Resource.Vanilla(name)) ?: return false
				// UPDATE register(NullRecipe().apply { useVanillaName(removed) })
				return true
			}
			
			IntegrityCheck.removedEnderChestRecipe = removeVanilla("ender_chest")
			IntegrityCheck.removedPurpurRecipe = removeVanilla("purpur_block")
			IntegrityCheck.removedEndRodRecipe = removeVanilla("end_rod")
			IntegrityCheck.removedEyeOfEnderRecipe = removeVanilla("ender_eye")
			// TODO figure out what to do with the broken advancements
			
			// ItemEnergyOracle.setupRecipeNBT(getValue(Resource.Custom("energy_oracle"))!!.recipeOutput)
			// ItemSpatialDashGem.setupRecipeNBT(getValue(Resource.Custom("spatial_dash_gem"))!!.recipeOutput)
			// UPDATE: hopfully figure out a better way to do this
			
			register(RecipeBindingEssence.serializer named "dynamic_binding_essence")
			register(RecipeEndPowderRepair.serializer named "dynamic_end_powder_repair")
			register(RecipeJarODustExtract.serializer named "dynamic_jar_o_dust_extract")
			register(RecipePortalTokenDuplication.serializer named "dynamic_portal_token_duplication")
			register(RecipeScaleOfFreefallRepair.serializer named "dynamic_scale_of_freefall_repair")
			register(RecipeVoidSalad.serializer named "dynamic_void_salad")
		}
	}
}
