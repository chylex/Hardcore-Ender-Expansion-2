package chylex.hee.game.particle

import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.particle.base.ParticleBaseFloating
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.toRadians
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import kotlin.math.PI

object ParticleEnderGoo : IParticleMaker.Simple() {
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ)
	}
	
	private val COLOR = FluidEnderGoo.rgbColor.let { floatArrayOf(it.redF, it.greenF, it.blueF) }
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBaseFloating(world, posX, posY, posZ, motX, motY, motZ) {
		private var angleOffset = (rand.nextInt(5, 10) * (if (rand.nextBoolean()) 1F else -1F)).toRadians()
		
		init {
			selectSpriteRandomly(ParticleEnderGoo.sprite)
			
			val colorMp = if (rand.nextInt(10) == 0)
				rand.nextFloat(0.3F, 0.6F)
			else
				0.9F
			
			particleRed   = COLOR[0] * colorMp * rand.nextFloat(0.8F, 1F)
			particleGreen = COLOR[1] * colorMp * rand.nextFloat(0.6F, 1F)
			particleBlue  = COLOR[2] * colorMp * rand.nextFloat(0.9F, 1F)
			
			particleAngle = rand.nextFloat(0.0, PI).toFloat()
			particleScale = rand.nextFloat(1.0F, 1.4F)
			
			particleGravity = 0.15F
			
			maxAge = rand.nextInt(34, 42)
		}
		
		override fun tick() {
			super.tick()
			
			prevParticleAngle = particleAngle
			particleAngle += angleOffset
			angleOffset *= 0.94F
			
			if (age > maxAge - 15) {
				particleScale *= 0.9F
			}
			
			motionX += rand.nextFloat(-0.02F, 0.02F)
			motionZ += rand.nextFloat(-0.02F, 0.02F)
		}
	}
}
