package chylex.hee.game.particle

import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.data.IParticleData
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.util.color.IColorGenerator
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import net.minecraft.client.particle.IParticleRenderType
import net.minecraft.client.particle.IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld
import java.util.Random

object ParticleGlitter : IParticleMaker.WithData<ParticleDataColorLifespanScale>() {
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		private val color: IColorGenerator,
		private val maxAgeMultiplier: IntRange,
	) : IParticleData<ParticleDataColorLifespanScale> {
		override fun generate(rand: Random): ParticleDataColorLifespanScale {
			return ParticleDataColorLifespanScale(color.next(rand), (4F / rand.nextFloat(0.1F, 1F)).toInt() * rand.nextInt(maxAgeMultiplier), rand.nextFloat(0.35F, 0.5F))
		}
	}
	
	@Sided(Side.CLIENT)
	private class Instance(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ) {
		init {
			selectSpriteRandomly(ParticleGlitter.sprite)
			
			if (data == null) {
				setExpired()
			}
			else {
				loadColor(data.color)
				particleAlpha = rand.nextFloat(0.1F, 1F)
				particleScale = data.scale
				
				maxAge = data.lifespan
			}
		}
		
		override fun tick() {
			super.tick()
			
			if (age < (maxAge * 3) / 4 && rand.nextInt(5) == 0) {
				particleAlpha = rand.nextFloat(0.5F, 1F)
			}
			
			particleAlpha -= 0.025F
		}
		
		override fun getRenderType(): IParticleRenderType {
			return PARTICLE_SHEET_TRANSLUCENT
		}
	}
}
