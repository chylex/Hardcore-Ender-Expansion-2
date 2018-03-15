package chylex.hee
import chylex.hee.init.ModConfig
import chylex.hee.proxy.ModCommonProxy
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger

@Mod(modid = HardcoreEnderExpansion.ID, useMetadata = true, modLanguageAdapter = "chylex.hee.system.core.KotlinAdapter", guiFactory = "chylex.hee.gui.ModGuiFactory")
object HardcoreEnderExpansion{
	const val ID = "hee"
	
	lateinit var log: Logger
	lateinit var config: ModConfig
	
    @EventHandler
	fun onPreInit(e: FMLPreInitializationEvent){
		log = e.modLog
		config = ModConfig(e.suggestedConfigurationFile)
	}
}
