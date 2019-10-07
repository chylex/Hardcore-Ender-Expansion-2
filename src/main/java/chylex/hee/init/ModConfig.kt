package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.ConfigElement
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.client.config.IConfigElement
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import java.io.File

class ModConfig(file: File){
	
	// Options
	
	class Client(config: Configuration, cat: String){
		val testOptionClient = config[cat, "testOptionClient", true].boolean // TODO remove
	}
	
	class General(config: Configuration, cat: String){
		val testOptionGeneral = config[cat, "testOptionGeneral", true].boolean // TODO remove
	}
	
	lateinit var client: Client
		private set
	
	lateinit var general: General
		private set
	
	// GUI
	
	val filePath: String
		get() = config.toString()
	
	val configElements: List<IConfigElement>
		get() = ConfigElement(config.getCategory(CATEGORY_CLIENT)).childElements
	
	// Internal
	
	companion object{
		private const val CATEGORY_GENERAL = "general"
		private const val CATEGORY_CLIENT = "client"
	}
	
	private val config = Configuration(file)
	
	init{
		MinecraftForge.EVENT_BUS.register(this)
		reload()
	}
	
	@SubscribeEvent
	fun onConfigChanged(e: ConfigChangedEvent.OnConfigChangedEvent){
		if (e.modID == HEE.ID){
			reload()
		}
	}
	
	private fun reload(){
		val clientOrDummy = if (FMLCommonHandler.instance().side == Side.CLIENT) config else Configuration()
		
		client = Client(clientOrDummy, CATEGORY_CLIENT)
		general = General(config, CATEGORY_GENERAL)
		
		if (config.hasChanged()){
			config.save()
		}
	}
}
