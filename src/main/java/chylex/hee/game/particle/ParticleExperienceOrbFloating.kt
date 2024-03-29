package chylex.hee.game.particle

import chylex.hee.game.particle.base.ParticleBase
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.util.color.IColorGenerator
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.Vec3
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

object ParticleExperienceOrbFloating : ParticleDataColorLifespanScale.ParticleMaker() {
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle {
		return Instance(world, posX, posY, posZ, motY, data.orDefault)
	}
	
	override val defaultColor = IColorGenerator { RGB(nextInt(0, 255), 255, nextInt(0, 51)) }
	override val defaultLifespan = 100..100
	override val defaultScale = 1F..1F
	
	@Sided(Side.CLIENT)
	class Instance(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motY: Double, data: ParticleDataColorLifespanScale) : ParticleBase(world, posX, posY, posZ, 0.0, 0.0, 0.0) {
		private val motionOffset: Double
		
		init {
			selectSpriteRandomly(ParticleExperienceOrbFloating.sprite)
			
			loadColor(data.color)
			particleScale = data.scale
			
			maxAge = data.lifespan
			
			motionVec = Vec3.y(motY)
			motionOffset = rand.nextFloat(-PI, PI)
		}
		
		override fun tick() {
			super.tick()
			
			motionX = sin(motionOffset + sign(motionOffset) * (age / 8.0)) * 0.02
			motionZ = cos(motionOffset + sign(motionOffset) * (age / 8.0)) * 0.02
			
			if (age > maxAge - 10) {
				particleAlpha -= 0.1F
			}
		}
	}
}
