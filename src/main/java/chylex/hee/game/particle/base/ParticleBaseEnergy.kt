package chylex.hee.game.particle.base
import chylex.hee.game.render.util.GL
import chylex.hee.system.Resource
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.Particle
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager.DestFactor.ONE
import net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
abstract class ParticleBaseEnergy(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ){
	private companion object{
		@JvmStatic private val TEX_PARTICLE = Resource.Custom("textures/particle/energy.png")
		
		@SideOnly(Side.CLIENT)
		private object TextureDescription : TextureAtlasSprite(TEX_PARTICLE.toString()){
			override fun getMinU(): Float = 0F
			override fun getMinV(): Float = 0F
			override fun getMaxU(): Float = 1F
			override fun getMaxV(): Float = 1F
		}
		
		private val mc = Minecraft.getMinecraft()
		private var lastInterpolationFixTime = 0L
	}
	
	init{
		particleTexture = TextureDescription
	}
	
	override fun renderParticle(buffer: BufferBuilder, entity: Entity, partialTicks: Float, rotationX: Float, rotationZ: Float, rotationYZ: Float, rotationXY: Float, rotationXZ: Float){
		val currentTime = Minecraft.getSystemTime()
		
		if (currentTime != lastInterpolationFixTime){
			lastInterpolationFixTime = currentTime
			
			// ParticleManager.renderLitParticles uses interpolation values from previous tick // UPDATE: did it get fixed?
			Particle.interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
			Particle.interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
			Particle.interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
			Particle.cameraViewDir = entity.getLook(partialTicks)
		}
		
		GL.enableBlend()
		GL.blendFunc(SRC_ALPHA, ONE)
		
		GL.disableAlpha()
		GL.disableLighting()
		GL.depthMask(false)
		
		GL.color(1F, 1F, 1F, 1F)
		
		mc.entityRenderer.setupFogColor(true)
		mc.renderEngine.bindTexture(TEX_PARTICLE)
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
		super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ)
		Tessellator.getInstance().draw()
		
		mc.entityRenderer.setupFogColor(false)
		
		GL.depthMask(true)
		GL.enableLighting()
		GL.enableAlpha()
		GL.disableBlend()
	}
	
	final override fun getFXLayer(): Int = 3
	final override fun shouldDisableDepth(): Boolean = true // doesn't do anything for getFXLayer == 3, but keep just in case
}
