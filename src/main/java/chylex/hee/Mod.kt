package chylex.hee

import chylex.hee.client.VanillaResourceOverrides
import chylex.hee.game.block.BlockBrewingStandCustom
import chylex.hee.game.block.BlockEndPortalOverride
import chylex.hee.game.block.BlockShulkerBoxOverride
import chylex.hee.game.block.properties.CustomPlantType
import chylex.hee.game.item.ItemShulkerBoxOverride
import chylex.hee.game.item.properties.CustomRarity
import chylex.hee.game.mechanics.causatum.EnderCausatum
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.game.territory.system.storage.PlayerTokenStorage
import chylex.hee.game.world.generation.EndChunkGenerator
import chylex.hee.game.world.generation.OverworldFeatures
import chylex.hee.init.ModCreativeTabs
import chylex.hee.init.ModLoot
import chylex.hee.init.ModPackets
import chylex.hee.init.ModPotions
import chylex.hee.init.ModTileEntities
import chylex.hee.network.NetworkManager
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraft.block.Blocks
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.DistExecutor.SafeRunnable
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent

@Mod(HEE.ID)
@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object Mod {
	init {
		with(ModLoadingContext.get()) {
			HEE.version = activeContainer.modInfo.version.toString()
		}
		
		try {
			Class.forName("chylex.hee.debug.Debug")
		} catch (e: ClassNotFoundException) {}
		
		@Suppress("ConvertLambdaToReference")
		DistExecutor.safeRunWhenOn(Side.CLIENT) {
			SafeRunnable { VanillaResourceOverrides.register() }
		}
		
		CustomRarity
		CustomPlantType
		ModCreativeTabs
	}
	
	@SubscribeEvent
	fun onCommonSetup(@Suppress("UNUSED_PARAMETER") e: FMLCommonSetupEvent) {
		NetworkManager.initialize(ModPackets.ALL)
		ModLoot
		
		e.enqueueWork {
			OverworldFeatures.registerConfiguredFeatures()
			EndChunkGenerator.registerCodec()
		}
		
		TrinketHandler.register()
		EnderCausatum.register()
		Instability.register()
		PlayerTokenStorage.register()
	}
	
	@SubscribeEvent
	fun onLoadComplete(@Suppress("UNUSED_PARAMETER") e: FMLLoadCompleteEvent) {
		ModPotions.setupVanillaOverrides()
		ModTileEntities.setupVanillaValidBlocks()
		IntegrityCheck.verify()
	}
	
	private object IntegrityCheck {
		fun verify() {
			crashIfFalse(Blocks.END_PORTAL::class.java === BlockEndPortalOverride::class.java, "invalid End Portal block: ${Blocks.END_PORTAL::class.java}")
			crashIfFalse(Blocks.BREWING_STAND::class.java === BlockBrewingStandCustom.Override::class.java, "invalid Brewing Stand block: ${Blocks.BREWING_STAND::class.java}")
			
			for (block in BlockShulkerBoxOverride.ALL_BLOCKS) {
				crashIfFalse(block.javaClass === BlockShulkerBoxOverride::class.java, "invalid Shulker Box block: ${block.javaClass}")
				crashIfFalse(block.asItem().javaClass === ItemShulkerBoxOverride::class.java, "invalid Shulker Box item: ${block.asItem().javaClass}")
			}
		}
		
		// Utilities
		
		private fun crashIfFalse(value: Boolean, message: String) {
			if (!value) {
				failIntegrityCheck(message, true)
			}
		}
		
		private fun failIntegrityCheck(message: String, crash: Boolean) {
			HEE.log.error("[IntegrityCheck] $message")
			check(!crash) { "Integrity check failed: $message" }
		}
	}
}
