package chylex.hee.game.world.feature.basic.caves.impl

import chylex.hee.game.world.feature.basic.caves.ICaveRadius
import java.util.Random
import kotlin.math.sin

class CaveRadiusSine(private val radius: Double, private val deviation: Double, private val frequency: Double, private val iterations: Int) : ICaveRadius {
	override fun next(rand: Random, step: Int): Double {
		var value = radius
		
		repeat(iterations) {
			value += sin((step + it) * frequency).coerceIn(-0.75, 0.75) * ((deviation / 0.75) / iterations)
		}
		
		return value
	}
}
