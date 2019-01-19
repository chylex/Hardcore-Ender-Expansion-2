package chylex.hee.game.particle.base
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * Particle with no gravity, no block collisions, and no motion randomness.
 */
@SideOnly(Side.CLIENT)
abstract class ParticleBaseFloating(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBase(world, posX, posY, posZ, motX, motY, motZ){
	init{
		motionX = motX
		motionY = motY
		motionZ = motZ
		particleGravity = 0F
	}
	
	override fun move(x: Double, y: Double, z: Double){ // skips collision checking
		boundingBox = boundingBox.offset(x, y, z)
		resetPositionToBB()
	}
	
	override fun multiplyVelocity(multiplier: Float): Particle = apply {
		motionX *= multiplier
		motionY *= multiplier
		motionZ *= multiplier
	}
}
