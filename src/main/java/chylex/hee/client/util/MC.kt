package chylex.hee.client.util
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.EntityPlayerSP
import chylex.hee.system.migration.vanilla.RenderManager
import net.minecraft.client.GameSettings
import net.minecraft.client.MainWindow
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Util

@Sided(Side.CLIENT)
object MC{
	@JvmField
	val instance: Minecraft = Minecraft.getInstance()
	
	// General
	
	val settings: GameSettings
		get() = instance.gameSettings
	
	val window: MainWindow
		get() = instance.mainWindow
	
	val systemTime
		get() = Util.milliTime()
	
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
	
	val renderManager: RenderManager
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
