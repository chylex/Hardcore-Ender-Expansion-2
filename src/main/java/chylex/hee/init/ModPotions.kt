package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.potion.PotionBanishment
import chylex.hee.game.potion.PotionCorruption
import chylex.hee.game.potion.PotionLifeless
import chylex.hee.game.potion.PotionPurity
import chylex.hee.game.potion.brewing.PotionBrewing
import chylex.hee.game.potion.brewing.PotionTypeMap
import chylex.hee.game.potion.brewing.recipes.BrewBasicEffects
import chylex.hee.game.potion.brewing.recipes.BrewUnalteredPotions
import chylex.hee.game.potion.brewing.recipes.BrewWaterToAwkward
import chylex.hee.game.potion.brewing.recipes.BrewWaterToMundane
import chylex.hee.game.potion.brewing.recipes.BrewWaterToThick
import chylex.hee.game.potion.brewing.recipes.ReinsertPotionItems
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.forge.getIfExists
import chylex.hee.system.forge.named
import chylex.hee.system.migration.Potion
import chylex.hee.system.migration.PotionType
import com.google.common.collect.ImmutableList
import net.minecraftforge.common.brewing.BrewingRecipeRegistry
import net.minecraftforge.common.brewing.IBrewingRecipe
import net.minecraftforge.common.brewing.VanillaBrewingRecipe
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModPotions{
	val LIFELESS   get() = PotionLifeless
	val PURITY     get() = PotionPurity
	val CORRUPTION get() = PotionCorruption
	val BANISHMENT get() = PotionBanishment
	
	private const val VANILLA_OVERRIDE_SUFFIX = "_no_effect_override"
	
	@SubscribeEvent
	fun onRegisterPotions(e: RegistryEvent.Register<Potion>){
		with(e.registry){
			register(LIFELESS named "lifeless")
			register(PURITY named "purity")
			register(CORRUPTION named "corruption")
			register(BANISHMENT named "banishment")
		}
	}
	
	@SubscribeEvent
	fun onRegisterTypes(e: RegistryEvent.Register<PotionType>){
		with(e.registry){
			register(PotionPurity.TYPE named "purity")
			register(PotionCorruption.TYPE named "corruption")
			register(PotionBanishment.TYPE named "banishment")
			
			val alteredTypes = PotionTypeMap.ALTERED_TYPES.map { it.registryName!!.path }.toSet()
			
			for(type in this){
				val location = type.registryName!!
				val path = location.path
				
				if ((Resource.isVanilla(location) || Resource.isCustom(location)) && alteredTypes.contains(path) && type.baseName == null && type.effects.isNotEmpty()){
					val info = PotionBrewing.INFO[type.effects[0].potion]
					
					if (info != null){
						
						// custom brewing system uses NBT to apply duration/level modifiers
						// to disable the original effect, each potion type is duplicated with no base effects
						
						val override = PotionType(path) named "$path$VANILLA_OVERRIDE_SUFFIX"
						register(override)
						
						// change base effects for vanilla potions to avoid breaking creative menu and compatibility w/ original items
						// register type overrides so that brewing recipes can convert them
						
						type.effects = ImmutableList.of(info.baseEffect)
						PotionTypeMap.registerNoEffectOverride(type, override)
						
						getIfExists(Resource.Vanilla("strong_$path"))?.let {
							it.effects = ImmutableList.of(info.vanillaOverrideStrongEffect)
							PotionTypeMap.registerNoEffectOverride(it, override)
						}
						
						getIfExists(Resource.Vanilla("long_$path"))?.let {
							it.effects = ImmutableList.of(info.vanillaOverrideLongEffect)
							PotionTypeMap.registerNoEffectOverride(it, override)
						}
					}
				}
			}
		}
	}
	
	@JvmStatic
	@Suppress("unused")
	fun excludeFromCreativeMenu(potion: PotionType): Boolean{
		return potion.registryName?.let { it.namespace == HEE.ID && it.path.endsWith(VANILLA_OVERRIDE_SUFFIX) } == true
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
}
