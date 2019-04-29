package chylex.hee.game.world.feature.basic.caves.impl
import chylex.hee.game.world.feature.basic.caves.ICaveRadius
import java.util.Random

class CaveRadiusStatic(private val radius: Double) : ICaveRadius{
	override fun next(rand: Random, step: Int): Double{
		return radius
	}
}
