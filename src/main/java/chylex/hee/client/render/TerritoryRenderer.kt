package chylex.hee.client.render

import chylex.hee.HEE
import chylex.hee.client.render.util.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.DF_ZERO
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.SF_ONE
import chylex.hee.client.render.util.SF_SRC_ALPHA
import chylex.hee.client.util.MC
import chylex.hee.game.entity.util.lookDirVec
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.particle.ParticleVoid
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.territory.TerritoryType
import chylex.hee.game.territory.TerritoryVoid
import chylex.hee.game.territory.system.properties.TerritoryEnvironment
import chylex.hee.game.world.isInEndDimension
import chylex.hee.util.color.IntColor
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.math.LerpedFloat
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.remapRange
import chylex.hee.util.math.scale
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.platform.GlStateManager.FogMode.EXP2
import net.minecraft.client.resources.I18n
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.vector.Vector3f
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.TickEvent.ClientTickEvent
import net.minecraftforge.event.TickEvent.Phase
import org.lwjgl.opengl.GL11.GL_GREATER
import kotlin.math.min
import kotlin.math.pow

@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
object TerritoryRenderer {
	@JvmStatic
	val isActive
		get() = MC.player.let { it != null && it.isInEndDimension }
	
	@JvmStatic
	val environment
		get() = MC.player?.takeIf(Entity::isInEndDimension)?.let { TerritoryType.fromX(it.posX.floorToInt()) }?.desc?.environment
	
	@JvmStatic
	val skyColor
		// use fog color because vanilla blends fog into sky color based on chunk render distance
		get() = environment?.let(TerritoryEnvironment::fogColor)
	
	@JvmStatic
	val celestialAngle
		get() = environment?.celestialAngle
	
	@JvmStatic
	val lightTable
		get() = environment?.lightBrightnessTable
	
	@JvmStatic
	fun updateLightmap(partialTicks: Float, sunBrightness: Float, blockLight: Float, skyLight: Float, colors: Vector3f) {
		// POLISH fix slightly weird edges compared to 1.14
		environment?.let { it.lightmap.update(colors, sunBrightness, skyLight.coerceAtMost(it.skyLight / 16F), blockLight, partialTicks) }
	}
	
	var debug = false
	
	private var prevChunkX = Int.MAX_VALUE
	private var prevTerritory: TerritoryType? = null
	
	@SubscribeEvent
	fun onClientTick(e: ClientTickEvent) {
		if (e.phase == Phase.START) {
			val player = MC.player
			
			if (player != null && player.isInEndDimension && player.ticksExisted > 0) {
				Void.tick(player)
				Title.tick()
				
				val newChunkX = player.chunkCoordX
				
				if (prevChunkX != newChunkX) {
					prevChunkX = newChunkX
					
					val newTerritory = TerritoryType.fromX(player.posX.floorToInt())
					
					if (prevTerritory != newTerritory) {
						prevTerritory = newTerritory
						
						if (newTerritory != null) {
							Void.reset()
							Title.display(newTerritory)
							newTerritory.desc.environment.setupClient(player)
						}
					}
				}
				
				prevTerritory?.desc?.environment?.tickClient(player)
			}
			else if (prevTerritory != null) {
				prevTerritory = null
				prevChunkX = Int.MAX_VALUE
				
				Void.reset()
				Title.reset()
			}
		}
	}
	
	// Fog rendering
	
	private val currentFogDensityMp
		get() = 1F + (9F * remapRange(currentVoidFactor, (-0.5F)..(1F), (0F)..(1F)).coerceIn(0F, 1F).pow(1.5F))
	
	private val currentRenderDistanceMp
		get() = MC.settings.renderDistanceChunks.let { if (it > 12) 0F else (1F - (it / 16.5F)).pow((it - 1) * 0.25F) }
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun onFog(@Suppress("UNUSED_PARAMETER") e: RenderFogEvent) {
		val player = MC.player?.takeIf(Entity::isInEndDimension) ?: return
		val territory = TerritoryType.fromPos(player)
		
		if (territory == null || TerritoryVoid.debug) {
			GL.setFogMode(EXP2)
			GL.setFogDensity(0F)
		}
		else {
			val env = territory.desc.environment
			val density = env.fogDensity * currentFogDensityMp
			val modifier = env.fogRenderDistanceModifier * currentRenderDistanceMp
			
			GL.setFogMode(EXP2)
			GL.setFogDensity(density + modifier)
		}
	}
	
	// Void handling
	
	val currentVoidFactor
		get() = Void.voidFactor.get(MC.partialTicks)
	
	val currentSkyAlpha
		get() = remapRange(currentVoidFactor, (-1F)..(0.5F), (1F)..(0F)).coerceIn(0F, 1F)
	
	private object Void {
		private val VOID_PARTICLE = ParticleSpawnerCustom(
			type = ParticleVoid,
			pos = InBox(8F)
		)
		
		val voidFactor = LerpedFloat(TerritoryVoid.OUTSIDE_VOID_FACTOR)
		
		fun tick(player: PlayerEntity) {
			val factor = TerritoryVoid.getVoidFactor(player).also(voidFactor::update)
			
			if (factor == TerritoryVoid.OUTSIDE_VOID_FACTOR || MC.instance.isGamePaused) {
				return
			}
			
			if (factor > TerritoryVoid.OUTSIDE_VOID_FACTOR) {
				val rand = player.rng
				
				val mp = min(1F, (factor * 0.275F) + 0.275F)
				val extra = (mp.pow(1.5F) * 12F).floorToInt()
				
				VOID_PARTICLE.spawn(Point(player, heightMp = 0.5F, amount = 2 + extra), rand)
				VOID_PARTICLE.spawn(Point(player.posVec.add(player.lookDirVec.scale(5)), amount = extra / 2), rand)
			}
		}
		
		fun reset() {
			voidFactor.updateImmediately(TerritoryVoid.OUTSIDE_VOID_FACTOR)
		}
	}
	
	// Text rendering
	
	@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
	private object Title {
		private const val FADE_TICKS = 22
		
		private var textTime = 0
		private var textFade = 0
		private var textTitle = ""
		private var textMainColor = RGB(0u)
		private var textShadowColor = RGB(0u)
		
		fun display(newTerritory: TerritoryType) {
			val title = I18n.format(newTerritory.translationKey)
			
			textTime = 60 + (2 * title.length)
			textFade = FADE_TICKS
			textTitle = title
			
			with(newTerritory.desc.colors) {
				if (tokenTop.asVec.lengthSquared() > tokenBottom.asVec.lengthSquared()) {
					textMainColor = tokenTop
					textShadowColor = tokenBottom
				}
				else {
					textMainColor = tokenBottom
					textShadowColor = tokenTop
				}
				
				textShadowColor = textShadowColor.let { RGB(it.red / 2, it.green / 2, it.blue / 2) }
			}
		}
		
		fun tick() {
			if (textTime > 0) {
				--textTime
				--textFade
			}
		}
		
		fun reset() {
			textTime = 0
		}
		
		@SubscribeEvent(EventPriority.HIGHEST)
		fun onRenderGameOverlayText(e: RenderGameOverlayEvent.Text) {
			if (textTime == 0) {
				return
			}
			
			val fontRenderer = MC.fontRenderer
			val resolution = e.window
			val width = resolution.scaledWidth
			val height = resolution.scaledHeight
			
			val opacity = when {
				textFade > 0          -> ((FADE_TICKS - (textFade - e.partialTicks)) / FADE_TICKS.toFloat()).coerceIn(0F, 1F)
				textTime < FADE_TICKS -> ((textTime - e.partialTicks) / FADE_TICKS.toFloat()).coerceIn(0F, 1F)
				else                  -> 1F
			}
			
			val matrix = e.matrixStack
			matrix.push()
			matrix.translate(width * 0.5, height * 0.5, 0.0)
			GL.enableBlend()
			GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA, SF_ONE, DF_ZERO)
			GL.alphaFunc(GL_GREATER, 0F)
			matrix.push()
			matrix.scale(3F, 3F, 3F)
			
			val x = -fontRenderer.getStringWidth(textTitle) * 0.5F
			val y = -fontRenderer.FONT_HEIGHT - 2F
			
			drawTitle(matrix, x + 0.5F, y + 0.5F, textShadowColor.withAlpha(opacity.pow(1.25F)))
			drawTitle(matrix, x, y, textMainColor.withAlpha(opacity))
			
			matrix.pop()
			GL.alphaFunc(GL_GREATER, 0.1F)
			GL.disableBlend()
			matrix.pop()
		}
		
		private fun drawTitle(matrix: MatrixStack, x: Float, y: Float, color: IntColor) {
			if (color.alpha > 3) { // prevents flickering alpha
				MC.fontRenderer.drawString(matrix, textTitle, x, y, color.i)
			}
		}
	}
}
