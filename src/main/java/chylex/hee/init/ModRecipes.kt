package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.item.ItemEnergyOracle
import chylex.hee.game.item.ItemSpatialDashGem
import chylex.hee.game.recipe.NullRecipe
import chylex.hee.game.recipe.RecipeBindingEssence
import chylex.hee.game.recipe.RecipeEndPowderRepair
import chylex.hee.game.recipe.RecipeJarODustExtract
import chylex.hee.game.recipe.RecipeScaleOfFreefallRepair
import chylex.hee.game.recipe.RecipeVoidSalad
import chylex.hee.system.IntegrityCheck
import chylex.hee.system.Resource
import chylex.hee.system.util.useVanillaName
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.registries.IForgeRegistryModifiable

@EventBusSubscriber(modid = HEE.ID)
object ModRecipes{
	fun initialize(){ // UPDATE: Move smelting recipes to JSON
		with(FurnaceRecipes.instance()){
			addSmeltingRecipeForBlock(ModBlocks.GLOOMROCK, ItemStack(ModBlocks.GLOOMROCK_SMOOTH), 0.1F)
			addSmeltingRecipeForBlock(ModBlocks.DUSTY_STONE_BRICKS, ItemStack(ModBlocks.DUSTY_STONE_CRACKED_BRICKS), 0.1F)
			addSmeltingRecipeForBlock(ModBlocks.DUSTY_STONE, ItemStack(ModBlocks.INFUSED_GLASS), 0.15F)
			addSmeltingRecipeForBlock(ModBlocks.END_POWDER_ORE, ItemStack(ModItems.END_POWDER), 0.15F)
			addSmeltingRecipeForBlock(ModBlocks.ENDIUM_ORE, ItemStack(ModItems.ENDIUM_INGOT), 1.0F)
			addSmeltingRecipeForBlock(ModBlocks.IGNEOUS_ROCK_ORE, ItemStack(ModItems.IGNEOUS_ROCK), 0.2F)
			
			addSmeltingRecipeForBlock(Blocks.VINE, ItemStack(ModBlocks.DRY_VINES), 0.1F)
			
			IntegrityCheck.removedChorusFruitRecipe = smeltingList.remove(smeltingList.keys.find { it.item === Items.CHORUS_FRUIT }) != null
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<IRecipe>){
		with(e.registry as? IForgeRegistryModifiable<IRecipe> ?: return){
			fun removeVanilla(name: String): Boolean{
				val removed = remove(Resource.Vanilla(name)) ?: return false
				
				register(NullRecipe().apply { useVanillaName(removed) })
				return true
			}
			
			IntegrityCheck.removedEnderChestRecipe = removeVanilla("ender_chest")
			IntegrityCheck.removedPurpurRecipe = removeVanilla("purpur_block")
			IntegrityCheck.removedEndRodRecipe = removeVanilla("end_rod")
			IntegrityCheck.removedEyeOfEnderRecipe = removeVanilla("ender_eye")
			// TODO figure out what to do with the broken advancements
			
			ItemEnergyOracle.setupRecipeNBT(getValue(Resource.Custom("energy_oracle"))!!.recipeOutput)
			ItemSpatialDashGem.setupRecipeNBT(getValue(Resource.Custom("spatial_dash_gem"))!!.recipeOutput)
			// UPDATE: hopfully figure out a better way to do this
			
			register(RecipeBindingEssence named "binding_essence")
			register(RecipeEndPowderRepair named "end_powder_repair")
			register(RecipeJarODustExtract named "jar_o_dust_extract")
			register(RecipeScaleOfFreefallRepair named "scale_of_freefall_repair")
			register(RecipeVoidSalad named "void_salad")
		}
	}
	
	// Utilities
	
	private infix fun IRecipe.named(registryName: String) = apply {
		setRegistryName(Resource.Custom(registryName))
	}
}
