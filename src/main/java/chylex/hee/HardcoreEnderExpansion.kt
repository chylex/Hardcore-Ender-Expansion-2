package chylex.hee
import chylex.hee.game.commands.HeeServerCommand
import chylex.hee.init.ModConfig
import chylex.hee.proxy.ModCommonProxy
import chylex.hee.system.Debug
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.network.NetworkCheckHandler
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.Logger

@Mod(modid = HardcoreEnderExpansion.ID, useMetadata = true, modLanguageAdapter = "chylex.hee.system.core.KotlinAdapter", guiFactory = "chylex.hee.gui.ModGuiFactory")
object HardcoreEnderExpansion{
	const val ID = "hee"
	
	lateinit var log: Logger
	lateinit var version: String
	lateinit var config: ModConfig
	
	@SidedProxy(clientSide = "chylex.hee.proxy.ModClientProxy", serverSide = "chylex.hee.proxy.ModCommonProxy")
	lateinit var proxy: ModCommonProxy
	
	@EventHandler
	fun onPreInit(e: FMLPreInitializationEvent){
		log = e.modLog
		version = e.modMetadata.version
		config = ModConfig(e.suggestedConfigurationFile)
		
		Debug.initialize()
	}
	
	@EventHandler
	fun onInit(e: FMLInitializationEvent){
		proxy.onInit()
	}
	
	@EventHandler
	fun onServerStarting(e: FMLServerStartingEvent){
		e.registerServerCommand(HeeServerCommand)
	}
	
	@NetworkCheckHandler
	fun onNetworkCheck(mods: Map<String, String>, @Suppress("UNUSED_PARAMETER") side: Side): Boolean{
		return mods[ID] == version
	}
}
