package chylex.hee.client.util
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.client.renderer.RenderItem
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.settings.GameSettings
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object MC{
	@JvmField
	val instance: Minecraft = Minecraft.getMinecraft()
	
	// General
	
	val settings: GameSettings
		get() = instance.gameSettings
	
	val resolution
		get() = ScaledResolution(instance)
	
	val systemTime
		get() = Minecraft.getSystemTime()
	
	// Game state
	
	val player: EntityPlayerSP?
		get() = instance.player
	
	val world: WorldClient?
		get() = instance.world
	
	val currentScreen: GuiScreen?
		get() = instance.currentScreen
	
	// Rendering
	
	val partialTicks: Float
		get() = instance.renderPartialTicks
	
	val textureManager: TextureManager
		get() = instance.textureManager
	
	val renderManager: RenderManager
		get() = instance.renderManager
	
	val particleManager: ParticleManager
		get() = instance.effectRenderer
	
	val entityRenderer: EntityRenderer
		get() = instance.entityRenderer
	
	val itemRenderer: RenderItem
		get() = instance.renderItem
	
	val fontRenderer: FontRenderer
		get() = instance.fontRenderer
}
