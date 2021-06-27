package chylex.hee.game.particle.base

import net.minecraft.client.world.ClientWorld

abstract class ParticleBaseHit(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBase(world, posX, posY, posZ, motX, motY, motZ) {
	init {
		maxAge = (6.0 / (Math.random() * 0.8 + 0.6)).toInt().coerceAtLeast(1)
		canCollide = false
		
		motionX = motX
		motionY = motY
		motionZ = motZ
	}
	
	override fun getScale(partialTicks: Float): Float {
		return particleScale * ((age + partialTicks) / maxAge.toFloat() * 32F).coerceIn(0F, 1F)
	}
	
	override fun tick() {
		prevPosX = posX
		prevPosY = posY
		prevPosZ = posZ
		
		if (age++ >= maxAge) {
			setExpired()
			return
		}
		
		particleRed *= 0.99F
		particleGreen *= 0.99F
		particleBlue *= 0.99F
		
		move(motionX, motionY, motionZ)
		motionX *= 0.9
		motionY *= 0.9
		motionZ *= 0.9
		motionY -= particleGravity
		
		if (onGround) {
			motionX *= 0.7
			motionZ *= 0.7
		}
	}
}
