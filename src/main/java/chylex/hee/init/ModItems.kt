package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.item.ItemEnergyReceptacle
import chylex.hee.game.item.ItemIgneousRock
import net.minecraft.item.Item
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HardcoreEnderExpansion.ID)
object ModItems{
	
	// Items: Raw resources
	
	@JvmField val ETHEREUM          = Item().apply { setup("ethereum") }
	@JvmField val ANCIENT_DUST      = Item().apply { setup("ancient_dust") }
	@JvmField val END_POWDER        = Item().apply { setup("end_powder") }
	@JvmField val STARDUST          = Item().apply { setup("stardust") }
	@JvmField val ENDIUM_INGOT      = Item().apply { setup("endium_ingot") }
	@JvmField val ENDIUM_NUGGET     = Item().apply { setup("endium_nugget") }
	@JvmField val OBSIDIAN_FRAGMENT = Item().apply { setup("obsidian_fragment") }
	@JvmField val IGNEOUS_ROCK      = ItemIgneousRock().apply { setup("igneous_rock") }
	@JvmField val PUZZLE_MEDALLION  = Item().apply { setup("puzzle_medallion") }
	@JvmField val INFERNIUM         = Item().apply { setup("infernium") }
	@JvmField val AURICION          = Item().apply { setup("auricion") }
	@JvmField val DRAGON_SCALE      = Item().apply { setup("dragon_scale") }
	
	// Items: Manufactured resources
	
	@JvmField val ALTERATION_NEXUS = Item().apply { setup("alteration_nexus") }
	@JvmField val VOID_ESSENCE     = Item().apply { setup("void_essence") }
	@JvmField val OBSIDIAN_ROD     = Item().apply { setup("obsidian_rod") }
	
	// Items: Energy
	
	@JvmField val ENERGY_RECEPTACLE = ItemEnergyReceptacle().apply { setup("energy_receptacle") }
	
	// Registry
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<Item>){
		with(e.registry){
			register(ETHEREUM)
			register(ANCIENT_DUST)
			register(END_POWDER)
			register(STARDUST)
			register(ENDIUM_INGOT)
			register(ENDIUM_NUGGET)
			register(OBSIDIAN_FRAGMENT)
			register(IGNEOUS_ROCK)
			register(PUZZLE_MEDALLION)
			register(INFERNIUM)
			register(AURICION)
			register(DRAGON_SCALE)
			
			register(ALTERATION_NEXUS)
			register(VOID_ESSENCE)
			register(OBSIDIAN_ROD)
			
			register(ENERGY_RECEPTACLE)
		}
	}
	
	// Utilities
	
	private fun Item.setup(registryName: String, unlocalizedName: String = "", inCreativeTab: Boolean = true){
		this.setRegistryName(HardcoreEnderExpansion.ID, registryName)
		this.unlocalizedName = "hee.${if (unlocalizedName.isEmpty()) registryName else unlocalizedName}"
		
		if (inCreativeTab){
			this.creativeTab = ModCreativeTabs.main.also { it.registerOrder(this) }
		}
	}
}
