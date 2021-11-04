package chylex.hee.game.particle

import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.util.color.IColorGenerator
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.random.nextInt
import net.minecraft.client.particle.IParticleRenderType
import net.minecraft.client.particle.IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld

object ParticleFadingSpot : ParticleDataColorLifespanScale.ParticleMaker() {
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	override val defaultColor: IColorGenerator = RGB(0u)
	override val defaultLifespan = 0..0
	override val defaultScale = 1F..1F
	
	@Sided(Side.CLIENT)
	private class Instance(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ) {
		private val alphaPerTick: Float
		private val scalePerTick: Float
		
		init {
			selectSpriteRandomly(ParticleFadingSpot.sprite)
			
			if (data == null) {
				alphaPerTick = 0F
				scalePerTick = 0F
				setExpired()
			}
			else {
				loadColor(data.color)
				particleScale = data.scale
				
				maxAge = data.lifespan
				
				alphaPerTick = 1F / maxAge
				scalePerTick = particleScale / (maxAge + rand.nextInt(1, 9))
			}
		}
		
		override fun tick() {
			super.tick()
			
			particleAlpha -= alphaPerTick
			particleScale -= scalePerTick
		}
		
		override fun getRenderType(): IParticleRenderType {
			return PARTICLE_SHEET_TRANSLUCENT
		}
	}
}
