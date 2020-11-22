package chylex.hee
import chylex.hee.game.block.BlockBrewingStandCustom
import chylex.hee.game.block.BlockEndPortalOverride
import chylex.hee.game.block.BlockShulkerBoxOverride
import chylex.hee.game.block.properties.CustomPlantType
import chylex.hee.game.entity.living.EntityMobEnderman
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.game.item.properties.CustomRarity
import chylex.hee.game.mechanics.causatum.EnderCausatum
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.game.world.feature.OverworldFeatures
import chylex.hee.game.world.territory.storage.TokenPlayerStorage
import chylex.hee.init.ModCreativeTabs
import chylex.hee.init.ModLoot
import chylex.hee.init.ModPackets
import chylex.hee.init.ModPotions
import chylex.hee.init.ModTileEntities
import chylex.hee.network.NetworkManager
import chylex.hee.proxy.ModClientProxy
import chylex.hee.proxy.ModCommonProxy
import chylex.hee.system.Debug
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import net.minecraft.block.Blocks
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.DistExecutor.SafeSupplier
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent

@Mod(HEE.ID)
@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object Mod{
	init{
		with(ModLoadingContext.get()){
			HEE.version = activeContainer.modInfo.version.toString()
		}
		
		@Suppress("ConvertLambdaToReference")
		HEE.proxy = DistExecutor.safeRunForDist(
			{ SafeSupplier { ModClientProxy() }},
			{ SafeSupplier { ModCommonProxy() }}
		)
		
		CustomRarity
		CustomPlantType
		
		ModCreativeTabs.initialize()
		WorldProviderEndCustom.register()
	}
	
	@SubscribeEvent
	fun onClientSetup(@Suppress("UNUSED_PARAMETER") e: FMLClientSetupEvent){
		Debug.initializeClient()
	}
	
	@SubscribeEvent
	fun onCommonSetup(@Suppress("UNUSED_PARAMETER") e: FMLCommonSetupEvent){
		NetworkManager.initialize(ModPackets.ALL)
		ModLoot.initialize()
		
		TrinketHandler.register()
		EnderCausatum.register()
		Instability.register()
		TokenPlayerStorage.register()
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
	
	private object IntegrityCheck{
		fun verify(){
			crashIfFalse(Blocks.END_PORTAL::class.java === BlockEndPortalOverride::class.java, "invalid End Portal block: ${Blocks.END_PORTAL::class.java}")
			crashIfFalse(Blocks.BREWING_STAND::class.java === BlockBrewingStandCustom::class.java, "invalid Brewing Stand block: ${Blocks.BREWING_STAND::class.java}")
			
			for(block in BlockShulkerBoxOverride.ALL_BLOCKS){
				crashIfFalse(block.javaClass === BlockShulkerBoxOverride::class.java, "invalid Shulker Box block: ${block.javaClass}")
				crashIfFalse(block.asItem().javaClass === ItemShulkerBoxOverride::class.java, "invalid Shulker Box item: ${block.asItem().javaClass}")
			}
			
			crashIfFalse(DimensionType.THE_END.directory == WorldProviderEndCustom.SAVE_FOLDER, "invalid End dimension save directory: ${DimensionType.THE_END.directory}")
			crashIfFalse(DimensionType.THE_END.factory === WorldProviderEndCustom.CONSTRUCTOR, "invalid End dimension factory: ${DimensionType.THE_END.factory}")
			crashIfFalse(DimensionType.THE_END.hasSkyLight, "invalid End dimension property: hasSkyLight != true")
		}
		
		// Utilities
		
		private fun crashIfFalse(value: Boolean, message: String){
			if (!value){
				failIntegrityCheck(message, true)
			}
		}
		
		private fun failIntegrityCheck(message: String, crash: Boolean){
			HEE.log.error("[IntegrityCheck] $message")
			check(!crash){ "Integrity check failed: $message" }
		}
	}
}
