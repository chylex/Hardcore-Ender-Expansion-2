package chylex.hee.game.world.feature.basic.caves

import java.util.Random

interface ICaveRadius {
	fun next(rand: Random, step: Int): Double
}
