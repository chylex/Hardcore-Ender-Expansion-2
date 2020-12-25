package chylex.hee.game.particle

import chylex.hee.game.particle.base.ParticleBase
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.random.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.world.World

object ParticleBubbleCustom : IParticleMaker.Simple() {
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ)
	}
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBase(world, posX, posY, posZ, motX, motY, motZ) {
		init {
			selectSpriteRandomly(ParticleBubbleCustom.sprite)
			
			setSize(0.02F, 0.02F)
			particleScale *= rand.nextFloat(0.2F, 0.8F)
			
			motionVec = motionVec.scale(0.2).add(
				rand.nextFloat(-0.02, 0.02),
				rand.nextFloat(-0.02, 0.02),
				rand.nextFloat(-0.02, 0.02)
			)
			
			maxAge = (8.0 / rand.nextFloat(0.2, 1.0)).toInt()
		}
		
		override fun tick() {
			prevPosX = posX
			prevPosY = posY
			prevPosZ = posZ
			
			motionY += 0.002
			move(motionX, motionY, motionZ)
			
			motionX *= 0.85
			motionY *= 0.85
			motionZ *= 0.85
			
			if (--maxAge < 0) {
				setExpired()
			}
		}
	}
}
