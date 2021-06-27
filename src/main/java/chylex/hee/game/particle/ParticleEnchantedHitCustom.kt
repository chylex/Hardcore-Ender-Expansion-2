package chylex.hee.game.particle

import chylex.hee.game.particle.base.ParticleBaseHit
import chylex.hee.game.particle.data.ParticleDataColorLifespanScale
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.random.IRandomColor.Companion.IRandomColor
import chylex.hee.system.random.nextInt
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld

object ParticleEnchantedHitCustom : ParticleDataColorLifespanScale.ParticleMaker() {
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data.orDefault)
	}
	
	override val defaultColor = IRandomColor { RGB(nextInt(46, 69), nextInt(122, 184), nextInt(153, 230)) }
	override val defaultLifespan = (-1)..(-1)
	override val defaultScale = (1F)..(1F)
	
	@Sided(Side.CLIENT)
	private class Instance(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorLifespanScale) : ParticleBaseHit(world, posX, posY, posZ, motX, motY, motZ) {
		init {
			selectSpriteRandomly(ParticleEnchantedHitCustom.sprite)
			
			loadColor(data.color)
			particleScale = data.scale
			
			if (data.lifespan != -1) {
				maxAge = data.lifespan
			}
		}
	}
}
