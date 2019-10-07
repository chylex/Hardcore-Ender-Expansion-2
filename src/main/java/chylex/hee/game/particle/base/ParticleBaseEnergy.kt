package chylex.hee.game.particle.base
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.render.util.TESSELLATOR
import chylex.hee.client.util.MC
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride.POWERED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride.REVITALIZING
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.DAMAGED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.TIRED
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.UNSTABLE
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus.WEAKENED
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.world.World
import org.lwjgl.opengl.GL11.GL_QUADS
import java.util.Random

@Sided(Side.CLIENT)
abstract class ParticleBaseEnergy(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
	private companion object{
		private val TEX_PARTICLE = Resource.Custom("textures/particle/energy.png")
		
		@Sided(Side.CLIENT)
		private object TextureDescription : TextureAtlasSprite(TEX_PARTICLE.toString()){
			override fun getMinU() = 0F
			override fun getMinV() = 0F
			override fun getMaxU() = 1F
			override fun getMaxV() = 1F
		}
		
		private var lastInterpolationFixTime = 0L
		
		private val COLOR_GRAY = adjustColorComponents(RGB(60u))
		
		private fun adjustColorComponents(color: IntColor): IntColor{
			return RGB(color.red.coerceIn(64, 224), color.green.coerceIn(64, 224), color.blue.coerceIn(64, 224))
		}
	}
	
	data class ClusterParticleData(val color: Int, val scale: Float)
	
	class ClusterParticleDataGenerator(cluster: TileEntityEnergyCluster){
		private val level = cluster.energyLevel.floating.value
		private val capacity = cluster.energyBaseCapacity.floating.value
		private val health = cluster.currentHealth
		
		private val colorPrimary = adjustColorComponents(cluster.color.primary(100F, 42F))
		private val colorSecondary = adjustColorComponents(cluster.color.secondary(90F, 42F))
		
		fun next(rand: Random): ClusterParticleData{
			val useSecondaryHue = when(health){
				REVITALIZING, UNSTABLE -> true
				POWERED                -> rand.nextBoolean()
				else                   -> rand.nextInt(4) == 0
			}
			
			val turnGray = useSecondaryHue && when(health){
				WEAKENED          -> rand.nextInt(100) < 25
				TIRED             -> rand.nextInt(100) < 75
				DAMAGED, UNSTABLE -> true
				else              -> false
			}
			
			val finalColor = when{
				turnGray        -> COLOR_GRAY
				useSecondaryHue -> colorSecondary
				else            -> colorPrimary
			}
			
			val finalScale = when(useSecondaryHue){
				true  -> (0.6F + (capacity * 0.07F) + (level * 0.008F)) * (if (health == POWERED) 1.6F else 1F)
				false ->  0.5F + (capacity * 0.03F) + (level * 0.06F)
			}
			
			return ClusterParticleData(color = finalColor.i, scale = finalScale)
		}
	}
	
	init{
		particleTexture = TextureDescription
	}
	
	override fun renderParticle(buffer: BufferBuilder, entity: Entity, partialTicks: Float, rotationX: Float, rotationZ: Float, rotationYZ: Float, rotationXY: Float, rotationXZ: Float){
		val currentTime = MC.systemTime
		
		if (currentTime != lastInterpolationFixTime){
			lastInterpolationFixTime = currentTime
			
			// ParticleManager.renderLitParticles uses interpolation values from previous tick // UPDATE: did it get fixed?
			interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
			interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
			interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
			cameraViewDir = entity.getLook(partialTicks)
		}
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE)
		
		GL.disableAlpha()
		GL.disableLighting()
		GL.depthMask(false)
		
		GL.color(1F, 1F, 1F, 1F)
		
		MC.entityRenderer.setupFogColor(true)
		MC.textureManager.bindTexture(TEX_PARTICLE)
		
		buffer.begin(GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
		super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ)
		TESSELLATOR.draw()
		
		MC.entityRenderer.setupFogColor(false)
		
		GL.depthMask(true)
		GL.enableLighting()
		GL.enableAlpha()
		GL.disableBlend()
	}
	
	final override fun getFXLayer() = 3
	final override fun shouldDisableDepth() = true // doesn't do anything for getFXLayer == 3, but keep just in case
}
