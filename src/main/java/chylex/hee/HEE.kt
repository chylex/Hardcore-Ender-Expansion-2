package chylex.hee
import chylex.hee.game.block.util.CustomPlantType
import chylex.hee.game.entity.living.EntityMobEnderman
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler
import chylex.hee.game.item.util.CustomRarity
import chylex.hee.game.mechanics.causatum.EnderCausatum
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.game.world.feature.OverworldFeatures
import chylex.hee.game.world.territory.storage.TokenPlayerStorage
import chylex.hee.init.ModCreativeTabs
import chylex.hee.init.ModLoot
import chylex.hee.init.ModNetwork
import chylex.hee.init.ModPotions
import chylex.hee.init.ModRecipes
import chylex.hee.init.ModTileEntities
import chylex.hee.proxy.Environment
import chylex.hee.system.Debug
import chylex.hee.system.IntegrityCheck
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraft.world.dimension.DimensionType
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(HEE.ID)
@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object HEE{
	const val ID = "hee"
	
	val log: Logger = LogManager.getLogger("HardcoreEnderExpansion")
	val version: String
	
	val dim: DimensionType = DimensionType.THE_END
	val proxy = Environment.constructProxy()
	
	init{
		with(ModLoadingContext.get()){
			version = activeContainer.modInfo.version.toString()
		}
		
		CustomRarity
		CustomPlantType
		
		ModCreativeTabs.initialize()
		WorldProviderEndCustom.register()
	}
	
	@SubscribeEvent
	fun onCommonSetup(@Suppress("UNUSED_PARAMETER") e: FMLCommonSetupEvent){
		Debug.initialize()
		
		ModNetwork.initialize()
		ModLoot.initialize()
		ModRecipes.initialize()
		
		TrinketHandler.register()
		EnderCausatum.register()
		Instability.register()
		TokenPlayerStorage.register()
		OverworldFeatures.register()
	}
	
	@SubscribeEvent
	fun onLoadComplete(@Suppress("UNUSED_PARAMETER") e: FMLLoadCompleteEvent){
		EntityMobEnderman.setupBiomeSpawns()
		EndermanBlockHandler.setupCarriableBlocks()
		ModPotions.setupVanillaOverrides()
		ModTileEntities.setupVanillaValidBlocks()
		OverworldFeatures.setupVanillaOverrides()
		IntegrityCheck.verify()
	}
	
	@SubscribeEvent
	fun onServerStarting(@Suppress("UNUSED_PARAMETER") e: FMLServerStartingEvent){
		// UPDATE e.registerServerCommand(HeeServerCommand)
	}
}
