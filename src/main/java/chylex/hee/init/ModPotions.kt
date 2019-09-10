package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.mechanics.potion.PotionLifeless
import chylex.hee.game.mechanics.potion.PotionPurity
import chylex.hee.game.mechanics.potion.brewing.PotionBrewing
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import chylex.hee.game.mechanics.potion.brewing.recipes.BrewBasicEffects
import chylex.hee.game.mechanics.potion.brewing.recipes.BrewUnalteredPotions
import chylex.hee.game.mechanics.potion.brewing.recipes.BrewWaterToAwkward
import chylex.hee.game.mechanics.potion.brewing.recipes.BrewWaterToMundane
import chylex.hee.game.mechanics.potion.brewing.recipes.BrewWaterToThick
import chylex.hee.game.mechanics.potion.brewing.recipes.ReinsertPotionItems
import chylex.hee.system.Resource
import chylex.hee.system.util.getIfExists
import com.google.common.collect.ImmutableList
import net.minecraft.potion.Potion
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
	fun onRegisterPotions(e: RegistryEvent.Register<Potion>){
		with(e.registry){
			register(PotionLifeless named "lifeless")
			register(PotionPurity named "purity")
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onRegisterTypes(e: RegistryEvent.Register<PotionType>){
		with(e.registry){
			register(PotionPurity.TYPE named "purity")
			
			val alteredTypes = PotionItems.ALTERED_TYPES.map { it.registryName!!.path }.toSet()
			
			for(type in this){
				val location = type.registryName!!
				val domain = location.namespace
				val path = location.path
				
				if ((domain == Resource.Vanilla.domain || domain == Resource.Custom.domain) && alteredTypes.contains(path) && type.baseName == null && type.effects.isNotEmpty()){
					val info = PotionBrewing.INFO[type.effects[0].potion]
					
					if (info != null){
						
						// custom brewing system uses NBT to apply duration/level modifiers
						// to disable the original effect, each potion type is duplicated with no base effects
						
						val override = PotionType(path) named "${path}_no_effect_override"
						register(override) // TODO the duplicates still show up in creative menu etc
						
						// change base effects for vanilla potions to avoid breaking creative menu and compatibility w/ original items
						// register type overrides so that brewing recipes can convert them
						
						type.effects = ImmutableList.of(info.baseEffect)
						PotionItems.registerNoEffectOverride(type, override)
						
						getIfExists(Resource.Vanilla("strong_$path"))?.let {
							it.effects = ImmutableList.of(info.vanillaOverrideStrongEffect)
							PotionItems.registerNoEffectOverride(it, override)
						}
						
						getIfExists(Resource.Vanilla("long_$path"))?.let {
							it.effects = ImmutableList.of(info.vanillaOverrideLongEffect)
							PotionItems.registerNoEffectOverride(it, override)
						}
					}
				}
			}
		}
	}
	
	// Recipes
	
	fun setupVanillaOverrides(){
		@Suppress("UNCHECKED_CAST")
		val recipes = BrewingRecipeRegistry::class.java.getDeclaredField("recipes").also { it.isAccessible = true }.get(null) as ArrayList<IBrewingRecipe>
		
		with(recipes){
			check(isNotEmpty() && removeAt(0) is VanillaBrewingRecipe){ "could not find vanilla brewing recipes in the registry" }
			
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
	}
	
	// Utilities
	
	private infix fun Potion.named(registryName: String) = apply {
		setPotionName("effect.hee.$registryName")
		setRegistryName(Resource.Custom(registryName))
	}
	
	private infix fun PotionType.named(registryName: String) = apply {
		setRegistryName(Resource.Custom(registryName))
	}
}
