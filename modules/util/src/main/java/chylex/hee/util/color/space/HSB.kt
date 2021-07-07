package chylex.hee.util.color.space

import chylex.hee.util.color.IntColor
import chylex.hee.util.color.RGB
import chylex.hee.util.math.floorToInt

data class HSB(val hue: Float, val saturation: Float, val brightness: Float) {
	companion object {
		fun fromRGB(rgb: IntColor): HSB {
			val (r, g, b) = rgb
			
			val min = minOf(r, g, b)
			val max = maxOf(r, g, b)
			
			if (min == max) {
				return HSB(0F, 0F, 0F)
			}
			
			val d = u(max - min)
			
			val h = when (max) {
				r    -> 0F + (u(g - b) / d)
				g    -> 2F + (u(b - r) / d)
				b    -> 4F + (u(r - g) / d)
				else -> throw UnsupportedOperationException()
			}
			
			val hue = (h * 60F).let { if (it < 0F) it + 360F else it }
			val umax = u(max)
			
			return HSB(hue, d / umax, umax)
		}
		
		private fun c(component: Float): Int {
			return (component * 255F).floorToInt()
		}
		
		private fun u(component: Int): Float {
			return component / 255F
		}
	}
	
	val i
		get() = toRGB().i
	
	fun toRGB(): IntColor {
		if (saturation == 0F) {
			return RGB((brightness * 255F).floorToInt().toUByte())
		}
		
		val h = hue / 60F
		val i = h.floorToInt()
		val f = h - i
		
		val p = brightness * (1F - saturation)
		val q = brightness * (1F - (saturation * f))
		val t = brightness * (1F - (saturation * (1F - f)))
		
		val cobr = c(brightness)
		
		return when (h.toInt()) {
			0    -> RGB(cobr, c(t), c(p))
			1    -> RGB(c(q), cobr, c(p))
			2    -> RGB(c(p), cobr, c(t))
			3    -> RGB(c(p), c(q), cobr)
			4    -> RGB(c(t), c(p), cobr)
			5    -> RGB(cobr, c(p), c(q))
			else -> throw UnsupportedOperationException()
		}
	}
}
