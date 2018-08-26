package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import net.minecraft.item.Item
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HardcoreEnderExpansion.ID)
object ModItems{
	
	// Items: Resources
	
	@JvmField val ETHEREUM         = Item().apply { setup("ethereum") }
	@JvmField val ANCIENT_DUST     = Item().apply { setup("ancient_dust") }
	@JvmField val ALTERATION_NEXUS = Item().apply { setup("alteration_nexus") }
	
	// Registry
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<Item>){
		with(e.registry){
			register(ETHEREUM)
			register(ANCIENT_DUST)
			register(ALTERATION_NEXUS)
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
