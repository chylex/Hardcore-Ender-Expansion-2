package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import net.minecraft.item.Item
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HardcoreEnderExpansion.ID)
object ModItems{
	/* TODO val testItem = Item().apply {
		setName("testitem", "test")
	}*/
	
	// Registry
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<Item>){
		with(e.registry){
			// TODO
		}
	}
	
	// Utilities
	
	private fun Item.setName(registryName: String, unlocalizedName: String){
		this.setRegistryName(HardcoreEnderExpansion.ID, registryName)
		this.unlocalizedName = "item.hee.$unlocalizedName"
	}
}
