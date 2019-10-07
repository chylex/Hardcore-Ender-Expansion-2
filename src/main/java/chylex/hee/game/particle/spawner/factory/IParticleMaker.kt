package chylex.hee.game.particle.spawner.factory
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.particle.Particle
import net.minecraft.world.World

interface IParticleMaker{
	@Sided(Side.CLIENT)
	fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle
}
