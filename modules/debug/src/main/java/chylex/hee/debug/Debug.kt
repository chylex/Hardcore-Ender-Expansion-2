package chylex.hee.debug

import chylex.hee.HEE
import chylex.hee.client.BuildStick
import chylex.hee.client.DebugMenu
import chylex.hee.client.GameModeToggle
import chylex.hee.client.TerritoryVoidDebug
import chylex.hee.game.block.BlockScaffoldingDebug
import chylex.hee.game.command.client.CommandClientDebugToggles
import chylex.hee.game.command.client.CommandClientScaffolding
import chylex.hee.game.command.server.CommandServerInstability
import chylex.hee.game.command.server.CommandServerStructure
import chylex.hee.game.command.server.CommandServerTerritory
import chylex.hee.game.command.server.CommandServerTestWorld
import chylex.hee.system.IDebugModule
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
internal object Debug : IDebugModule {
	init {
		HEE.debug = true
		HEE.debugModule = this
	}
	
	override val clientCommands
		get() = listOf(
			CommandClientScaffolding,
			CommandClientDebugToggles
		)
	
	override val serverCommands
		get() = listOf(
			CommandServerInstability,
			CommandServerStructure,
			CommandServerTerritory,
			CommandServerTestWorld
		)
	
	override val scaffoldingBlockBehavior
		get() = BlockScaffoldingDebug
	
	@SubscribeEvent
	fun onClientSetup(@Suppress("UNUSED_PARAMETER") e: FMLClientSetupEvent) {
		initializeClient()
	}
	
	@Sided(Side.CLIENT)
	private fun initializeClient() {
		if (HEE.debug) {
			MinecraftForge.EVENT_BUS.register(DebugMenu)
			MinecraftForge.EVENT_BUS.register(BuildStick)
			MinecraftForge.EVENT_BUS.register(GameModeToggle)
			MinecraftForge.EVENT_BUS.register(TerritoryVoidDebug)
			
			MinecraftForge.EVENT_BUS.register(object : Any() {
				@SubscribeEvent
				fun onGuiOpen(@Suppress("UNUSED_PARAMETER") e: GuiOpenEvent) {
					PowerShell.maximizeWindow()
					MinecraftForge.EVENT_BUS.unregister(this)
				}
			})
		}
	}
}
