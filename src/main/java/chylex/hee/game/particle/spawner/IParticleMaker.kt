package chylex.hee.game.particle.spawner
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.particle.Particle
import net.minecraft.world.World

interface IParticleMaker<T>{
	@Sided(Side.CLIENT)
	fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: T?): Particle
	
	abstract class Simple : IParticleMaker<Unit>{
		@Sided(Side.CLIENT)
		final override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Unit?): Particle{
			return create(world, posX, posY, posZ, motX, motY, motZ)
		}
		
		@Sided(Side.CLIENT)
		abstract fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double): Particle
	}
}
