package chylex.hee.game.particle.spawner
import chylex.hee.game.particle.util.IShape
import java.util.Random

interface IParticleSpawner{
	fun spawn(shape: IShape, rand: Random)
}
