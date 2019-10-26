package chylex.hee.game.particle
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleBubble
import net.minecraft.world.World

object ParticleBubbleCustom : IParticleMaker.Simple(){
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ)
	}
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double) : ParticleBubble(world, posX, posY, posZ, motX, motY, motZ){
		override fun onUpdate(){
			prevPosX = posX
			prevPosY = posY
			prevPosZ = posZ
			
			motionY += 0.002
			move(motionX, motionY, motionZ)
			
			motionX *= 0.85
			motionY *= 0.85
			motionZ *= 0.85
			
			if (--particleMaxAge < 0){
				setExpired()
			}
		}
	}
}
