package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.ConfigElement
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.client.config.IConfigElement
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File

private const val CATEGORY_GENERAL = "general"
private const val CATEGORY_CLIENT = "client"

class ModConfig(file: File){
	// Options
	
	var testOptionClient: Boolean = true // TODO remove
		private set
	
	var testOptionGeneral: Boolean = true // TODO remove
		private set
	
	// GUI
	
	val filePath: String
		get() = config.toString()
	
	val configElements: List<IConfigElement>
		get() = ConfigElement(config.getCategory(CATEGORY_CLIENT)).childElements
	
	// Internal
	
	private val config = Configuration(file)
	
	init{
		MinecraftForge.EVENT_BUS.register(this)
		reload()
	}
	
	@SubscribeEvent
	fun onConfigChanged(e: ConfigChangedEvent.OnConfigChangedEvent){
		if (e.modID == HardcoreEnderExpansion.ID){
			reload()
		}
	}
	
	private fun reload(){
		testOptionClient = config[CATEGORY_CLIENT, "testOptionClient", testOptionClient].boolean
		testOptionGeneral = config[CATEGORY_GENERAL, "testOptionGeneral", testOptionGeneral].boolean
		
		if (config.hasChanged()){
			config.save()
		}
	}
}
