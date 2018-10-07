package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.mechanics.potion.PotionLifeless
import chylex.hee.system.Resource
import net.minecraft.potion.Potion
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
		}
	}
	
	// Utilities
	
	private infix fun Potion.named(registryName: String) = apply {
		setPotionName("effect.hee.$registryName")
		setRegistryName(Resource.Custom(registryName))
	}
}
