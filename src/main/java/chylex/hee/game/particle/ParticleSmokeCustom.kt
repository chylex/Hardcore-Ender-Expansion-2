package chylex.hee.game.particle

import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.floorToInt
import chylex.hee.system.random.IRandomColor.Companion.IRandomColor
import chylex.hee.system.random.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.SmokeParticle
import net.minecraft.world.World

object ParticleSmokeCustom : ParticleDataColorLifespanScale.ParticleMaker() {
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data.orDefault)
	}
	
	override val defaultColor = IRandomColor { RGB((nextFloat(0F, 0.3F) * 255F).floorToInt().toUByte()) }
	override val defaultLifespan = (-1)..(-1)
	override val defaultScale = (1F)..(1F)
	
	@Sided(Side.CLIENT)
	private class Instance(
		world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale,
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
