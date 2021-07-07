package chylex.hee.util.color

import java.util.Random

interface IColorGenerator {
	fun next(rand: Random): IntColor
}

fun IColorGenerator(generator: Random.() -> IntColor) = object : IColorGenerator {
	override fun next(rand: Random): IntColor {
		return generator(rand)
	}
}
