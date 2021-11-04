package chylex.hee.game.particle

import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.util.color.IColorGenerator
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.floorToInt
import chylex.hee.util.random.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.SmokeParticle
import net.minecraft.client.world.ClientWorld

object ParticleSmokeCustom : ParticleDataColorLifespanScale.ParticleMaker() {
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data.orDefault)
	}
	
	override val defaultColor = IColorGenerator { RGB((nextFloat(0F, 0.3F) * 255F).floorToInt().toUByte()) }
	override val defaultLifespan = (-1)..(-1)
	override val defaultScale = (1F)..(1F)
	
	@Sided(Side.CLIENT)
	private class Instance(
		world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale,
	) : SmokeParticle(
		world, posX, posY, posZ, motX, motY, motZ, data.scale, sprite
	) {
		init {
			val color = data.color
			
			particleRed = color.redF
			particleGreen = color.greenF
			particleBlue = color.blueF
			
			if (data.lifespan != -1) {
				maxAge = data.lifespan
			}
		}
	}
}
