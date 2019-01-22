package chylex.hee
import chylex.hee.game.commands.HeeServerCommand
import chylex.hee.game.entity.item.EntityItemIgneousRock
import chylex.hee.game.mechanics.TrinketHandler
import chylex.hee.game.mechanics.causatum.EnderCausatum
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.game.world.provider.WorldProviderEndCustom
import chylex.hee.init.ModConfig
import chylex.hee.init.ModCreativeTabs
import chylex.hee.init.ModGuiHandler
import chylex.hee.init.ModLoot
import chylex.hee.init.ModNetwork
import chylex.hee.init.ModRecipes
import chylex.hee.proxy.ModCommonProxy
import chylex.hee.system.Debug
import chylex.hee.system.IntegrityCheck
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.network.NetworkCheckHandler
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.Logger

@Mod(modid = HEE.ID, useMetadata = true, modLanguageAdapter = "chylex.hee.system.core.KotlinAdapter", guiFactory = "chylex.hee.init.factory.ConfigGuiFactory")
object HEE{
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
		ModNetwork.initialize()
		ModGuiHandler.initialize()
		ModCreativeTabs.initialize()
		TrinketHandler.register()
		EnderCausatum.register()
		Instability.register()
		WorldProviderEndCustom.register()
		proxy.onPreInit()
	}
	
	@EventHandler
	fun onInit(e: FMLInitializationEvent){
		ModLoot.initialize()
		ModRecipes.initialize()
		proxy.onInit()
	}
	
	@EventHandler
	fun onPostInit(e: FMLPostInitializationEvent){
		EntityItemIgneousRock.setupSmeltingTransformations()
	}
	
	@EventHandler
	fun onLoadComplete(e: FMLLoadCompleteEvent){
		IntegrityCheck.verify()
	}
	
	@EventHandler
	fun onServerStarting(e: FMLServerStartingEvent){
		e.registerServerCommand(HeeServerCommand)
	}
	
	@NetworkCheckHandler
	fun onNetworkCheck(mods: Map<String, String>, @Suppress("UNUSED_PARAMETER") side: Side): Boolean{
		return mods.isNotEmpty() && mods[ID] == version
	}
}
