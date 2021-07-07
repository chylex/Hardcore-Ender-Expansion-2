package chylex.hee.game.world.generation.cave

import java.util.Random

interface ICaveRadius {
	fun next(rand: Random, step: Int): Double
}
