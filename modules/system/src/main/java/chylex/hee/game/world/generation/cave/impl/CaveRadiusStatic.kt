package chylex.hee.game.world.generation.cave.impl

import chylex.hee.game.world.generation.cave.ICaveRadius
import java.util.Random

class CaveRadiusStatic(private val radius: Double) : ICaveRadius {
	override fun next(rand: Random, step: Int): Double {
		return radius
	}
}
