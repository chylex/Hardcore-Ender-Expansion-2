package chylex.hee
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger

@Mod(modid = "hee", useMetadata = true, modLanguageAdapter = "chylex.hee.system.core.KotlinAdapter")
object HardcoreEnderExpansion {
	const val ID = "hee"
	
	lateinit var log: Logger
	
    @EventHandler
	fun onPreInit(e: FMLPreInitializationEvent){
		log = e.modLog
	}
}
