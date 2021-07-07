package chylex.hee.game.particle.base

import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.world.ClientWorld

/**
 * Particle with no gravity, no block collisions, and no motion randomness.
 */
@Sided(Side.CLIENT)
abstract class ParticleBaseFloating(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBase(world, posX, posY, posZ, motX, motY, motZ) {
	init {
		motionX = motX
		motionY = motY
		motionZ = motZ
		particleGravity = 0F
	}
	
	override fun move(x: Double, y: Double, z: Double) { // skips collision checking
		boundingBox = boundingBox.offset(x, y, z)
		resetPositionToBB()
	}
	
	override fun multiplyVelocity(multiplier: Float) = apply {
		motionX *= multiplier
		motionY *= multiplier
		motionZ *= multiplier
	}
}
