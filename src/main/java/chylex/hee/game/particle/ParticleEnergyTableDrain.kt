package chylex.hee.game.particle

import chylex.hee.game.particle.base.ParticleBaseEnergy
import chylex.hee.game.particle.data.ParticleDataColorScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld

object ParticleEnergyTableDrain : IParticleMaker.WithData<ParticleDataColorScale>() {
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorScale?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	@Sided(Side.CLIENT)
	private class Instance(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorScale?) : ParticleBaseEnergy(world, posX, posY, posZ, motX, motY, motZ) {
		init {
			selectSpriteRandomly(ParticleEnergyTableDrain.sprite)
			
			if (data == null) {
				setExpired()
			}
			else {
				loadColor(data.color)
				particleAlpha = 1F
				particleScale = 0.45F + (data.scale * 0.15F)
				
				maxAge = 6
			}
		}
		
		override fun tick() {
			super.tick()
			
			particleAlpha -= 0.16F
		}
	}
}
