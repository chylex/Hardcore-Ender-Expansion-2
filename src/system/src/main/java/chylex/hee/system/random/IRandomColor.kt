package chylex.hee.system.random

import chylex.hee.system.color.IntColor
import java.util.Random

interface IRandomColor {
	fun next(rand: Random): IntColor
	
	class Static(private val color: IntColor) : IRandomColor {
		override fun next(rand: Random) = color
	}
	
	@Suppress("FunctionName")
	companion object {
		fun IRandomColor(generator: Random.() -> IntColor) = object : IRandomColor {
			override fun next(rand: Random): IntColor {
				return generator(rand)
			}
		}
	}
}
