package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.item.ItemEnergyOracle
import chylex.hee.game.item.ItemSpatialDashGem
import chylex.hee.system.IntegrityCheck
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.registries.IForgeRegistryModifiable

@EventBusSubscriber(modid = HardcoreEnderExpansion.ID)
object ModRecipes{
	fun initialize(){ // UPDATE: Move smelting recipes to JSON
		with(FurnaceRecipes.instance()){
			addSmeltingRecipeForBlock(ModBlocks.GLOOMROCK, ItemStack(ModBlocks.GLOOMROCK_SMOOTH), 0.1F)
			addSmeltingRecipeForBlock(ModBlocks.END_POWDER_ORE, ItemStack(ModItems.END_POWDER), 0.15F)
			addSmeltingRecipeForBlock(ModBlocks.ENDIUM_ORE, ItemStack(ModItems.ENDIUM_INGOT), 1.0F)
			addSmeltingRecipeForBlock(ModBlocks.IGNEOUS_ROCK_ORE, ItemStack(ModItems.IGNEOUS_ROCK), 0.2F)
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<IRecipe>){
		with(e.registry as? IForgeRegistryModifiable<IRecipe> ?: return){
			fun removeVanilla(name: String): Boolean = remove(ResourceLocation("minecraft", name)) != null
			
			IntegrityCheck.removedEnderChestRecipe = removeVanilla("ender_chest")
			IntegrityCheck.removedPurpurRecipe = removeVanilla("purpur_block")
			IntegrityCheck.removedEndRodRecipe = removeVanilla("end_rod")
			// TODO figure out what to do with the broken advancements
			
			ItemEnergyOracle.setupRecipeNBT(getValue(ResourceLocation(HardcoreEnderExpansion.ID, "energy_oracle"))!!)
			ItemSpatialDashGem.setupRecipeNBT(getValue(ResourceLocation(HardcoreEnderExpansion.ID, "spatial_dash_gem"))!!)
			// UPDATE: hopfully figure out a better way to do this
		}
	}
}
