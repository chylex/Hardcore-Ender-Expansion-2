package chylex.hee.game.particle

import chylex.hee.game.particle.data.ParticleDataColorScale
import chylex.hee.util.color.IColorGenerator
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.SpellParticle
import net.minecraft.client.world.ClientWorld

object ParticleSpellCustom : ParticleDataColorScale.ParticleMaker() {
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorScale?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	override val defaultColor: IColorGenerator = RGB(0u)
	override val defaultScale = (1F)..(1F)
	
	@Sided(Side.CLIENT)
	private class Instance(
		world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorScale?,
	) : SpellParticle(
		world, posX, posY, posZ, 0.0, 0.0, 0.0, sprite
	) {
		init {
			motionX = motX
			motionY = motY
			motionZ = motZ
			
			if (data == null) {
				setExpired()
			}
			else {
				val color = data.color
				
				particleRed = color.redF
				particleGreen = color.greenF
				particleBlue = color.blueF
				
				particleScale *= data.scale
			}
		}
	}
}
