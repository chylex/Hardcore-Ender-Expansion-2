package chylex.hee.game.particle.base

import chylex.hee.client.render.RenderStateBuilder
import chylex.hee.client.render.util.DF_ONE
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.SF_SRC_ALPHA
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride.POWERED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride.REVITALIZING
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.DAMAGED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.TIRED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.UNSTABLE
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.WEAKENED
import chylex.hee.game.particle.data.IParticleData
import chylex.hee.game.particle.data.ParticleDataColorScale
import chylex.hee.util.color.IntColor
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.particle.IParticleRenderType
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.world.ClientWorld
import org.lwjgl.opengl.GL11.GL_QUADS
import java.util.Random

@Sided(Side.CLIENT)
abstract class ParticleBaseEnergy(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ) {
	private companion object {
		private val COLOR_GRAY = adjustColorComponents(RGB(60u))
		
		private fun adjustColorComponents(color: IntColor): IntColor {
			return RGB(color.red.coerceIn(64, 224), color.green.coerceIn(64, 224), color.blue.coerceIn(64, 224))
		}
	}
	
	class ClusterParticleDataGenerator(cluster: TileEntityEnergyCluster) : IParticleData<ParticleDataColorScale> {
		private val level = cluster.energyLevel.floating.value
		private val capacity = cluster.energyBaseCapacity.floating.value
		private val health = cluster.currentHealth
		
		private val colorPrimary = adjustColorComponents(cluster.color.primary(100F, 42F))
		private val colorSecondary = adjustColorComponents(cluster.color.secondary(90F, 42F))
		
		override fun generate(rand: Random): ParticleDataColorScale {
			val useSecondaryHue = when (health) {
				REVITALIZING, UNSTABLE -> true
				POWERED                -> rand.nextBoolean()
				else                   -> rand.nextInt(4) == 0
			}
			
			val turnGray = useSecondaryHue && when (health) {
				WEAKENED          -> rand.nextInt(100) < 25
				TIRED             -> rand.nextInt(100) < 75
				DAMAGED, UNSTABLE -> true
				else              -> false
			}
			
			val finalColor = when {
				turnGray        -> COLOR_GRAY
				useSecondaryHue -> colorSecondary
				else            -> colorPrimary
			}
			
			val finalScale = when (useSecondaryHue) {
				true  -> (0.6F + (capacity * 0.07F) + (level * 0.008F)) * (if (health == POWERED) 1.6F else 1F)
				false -> 0.5F + (capacity * 0.03F) + (level * 0.06F)
			}
			
			return ParticleDataColorScale(color = finalColor, scale = finalScale)
		}
	}
	
	private object RenderType : IParticleRenderType {
		@Suppress("DEPRECATION")
		override fun beginRender(buffer: BufferBuilder, textureManager: TextureManager) {
			GL.enableBlend()
			GL.blendFunc(SF_SRC_ALPHA, DF_ONE)
			GL.disableAlpha()
			GL.disableLighting()
			GL.depthMask(false)
			GL.color(1F, 1F, 1F, 1F)
			RenderStateBuilder.FOG_BLACK.setupRenderState()
			
			GL.bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE)
			buffer.begin(GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
		}
		
		override fun finishRender(tessellator: Tessellator) {
			tessellator.draw()
			
			RenderStateBuilder.FOG_BLACK.clearRenderState()
			GL.depthMask(true)
			GL.enableLighting()
			GL.enableAlpha()
			GL.disableBlend()
			
			RenderSystem.enableColorMaterial() // UPDATE works around broken GL state when this render type runs before other types (otherwise causes gray particles)
		}
	}
	
	override fun getRenderType(): IParticleRenderType {
		return RenderType
	}
}
