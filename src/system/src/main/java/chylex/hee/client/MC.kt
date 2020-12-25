package chylex.hee.client

import chylex.hee.game.particle.ParticleSetting
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityPlayerSP
import net.minecraft.client.GameSettings
import net.minecraft.client.MainWindow
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.settings.ParticleStatus
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Util

@Sided(Side.CLIENT)
object MC {
	@JvmField
	val instance: Minecraft = Minecraft.getInstance()
	
	// General
	
	val window: MainWindow
		get() = instance.mainWindow
	
	val systemTime
		get() = Util.milliTime()
	
	// Settings
	
	val settings: GameSettings
		get() = instance.gameSettings
	
	val particleSetting
		get() = when(instance.gameSettings.particles) {
			ParticleStatus.MINIMAL   -> ParticleSetting.MINIMAL
			ParticleStatus.DECREASED -> ParticleSetting.DECREASED
			else                     -> ParticleSetting.ALL
		}
	
	// Game state
	
	val player: EntityPlayerSP?
		get() = instance.player
	
	val world: ClientWorld?
		get() = instance.world
	
	val currentScreen: Screen?
		get() = instance.currentScreen
	
	// Rendering
	
	val partialTicks: Float
		get() = instance.renderPartialTicks
	
	val textureManager: TextureManager
		get() = instance.textureManager
	
	val renderManager: EntityRendererManager
		get() = instance.renderManager
	
	val particleManager: ParticleManager
		get() = instance.particles
	
	val gameRenderer: GameRenderer
		get() = instance.gameRenderer
	
	val itemRenderer: ItemRenderer
		get() = instance.itemRenderer
	
	val fontRenderer: FontRenderer
		get() = instance.fontRenderer
}
