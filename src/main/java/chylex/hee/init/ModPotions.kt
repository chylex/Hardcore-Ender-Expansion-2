package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.mechanics.potion.PotionLifeless
import chylex.hee.game.mechanics.potion.PotionPurity
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import chylex.hee.game.mechanics.potion.brewing.recipes.BrewBasicEffects
import chylex.hee.game.mechanics.potion.brewing.recipes.BrewUnalteredPotions
import chylex.hee.game.mechanics.potion.brewing.recipes.BrewWaterToAwkward
import chylex.hee.game.mechanics.potion.brewing.recipes.BrewWaterToMundane
import chylex.hee.game.mechanics.potion.brewing.recipes.BrewWaterToThick
import chylex.hee.game.mechanics.potion.brewing.recipes.ReinsertPotionItems
import chylex.hee.system.Resource
import com.google.common.collect.ImmutableList
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.potion.PotionType
import net.minecraftforge.common.brewing.BrewingRecipeRegistry
import net.minecraftforge.common.brewing.IBrewingRecipe
import net.minecraftforge.common.brewing.VanillaBrewingRecipe
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HEE.ID)
object ModPotions{
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<Potion>){
		with(e.registry){
			register(PotionLifeless named "lifeless")
			register(PotionPurity named "purity")
		}
	}
	
	// Recipes
	
	fun setupVanillaOverrides(){
		@Suppress("UNCHECKED_CAST")
		val recipes = BrewingRecipeRegistry::class.java.getDeclaredField("recipes").also { it.isAccessible = true }.get(null) as ArrayList<IBrewingRecipe>
		
		with(recipes){
			if (isEmpty() || removeAt(0) !is VanillaBrewingRecipe){
				throw IllegalStateException("could not find vanilla brewing recipes in the registry")
			}
			
			addAll(0, listOf(
				BrewBasicEffects.FromAwkward,
				BrewBasicEffects.FromWater,
				BrewWaterToAwkward,
				BrewWaterToMundane,
				BrewWaterToThick,
				ReinsertPotionItems,
				BrewUnalteredPotions
			))
		}
		
		val vanillaTypes = PotionItems.VANILLA_TYPES.map { it.registryName!!.path }.toSet()
		val emptyEffects = ImmutableList.of<PotionEffect>()
		
		for(type in PotionType.REGISTRY){
			val location = type.registryName!!
			
			if (location.namespace == Resource.Vanilla.domain && (vanillaTypes.contains(location.path) || type.baseName?.let(vanillaTypes::contains) == true)){
				type.effects = emptyEffects // removes duplicate effects when using custom effects in NBT
			}
		}
	}
	
	// Utilities
	
	private infix fun Potion.named(registryName: String) = apply {
		setPotionName("effect.hee.$registryName")
		setRegistryName(Resource.Custom(registryName))
	}
}
