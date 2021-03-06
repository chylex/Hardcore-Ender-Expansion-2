package chylex.hee.game.particle

import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.random.IRandomColor
import chylex.hee.system.random.nextInt
import net.minecraft.client.particle.IParticleRenderType
import net.minecraft.client.particle.IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
import net.minecraft.client.particle.Particle
import net.minecraft.world.World

object ParticleFadingSpot : ParticleDataColorLifespanScale.ParticleMaker() {
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	override val defaultColor = IRandomColor.Static(RGB(0u))
	override val defaultLifespan = 0..0
	override val defaultScale = 1F..1F
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ) {
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
