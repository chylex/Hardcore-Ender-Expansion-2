package chylex.hee.game.particle.spawner
import chylex.hee.game.particle.spawner.properties.IShape
import java.util.Random

interface IParticleSpawner{
	fun spawn(shape: IShape, rand: Random)
}
