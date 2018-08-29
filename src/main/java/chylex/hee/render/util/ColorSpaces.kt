package chylex.hee.render.util
import chylex.hee.system.util.floorToInt
import org.lwjgl.util.Color
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

interface IColor{
	fun toRGB(): Color
	
	fun toInt(alpha: Float? = null): Int{
		val rgb = toRGB()
		return (if (alpha == null) 0 else ((alpha * 255).floorToInt() shl 24)) or (rgb.red shl 16) or (rgb.green shl 8) or rgb.blue
	}
}

/**
 * Represents an RGB color.
 *
 * @param[red] value between 0-255
 * @param[green] value between 0-255
 * @param[blue] value between 0-255
 */
data class RGB(val red: Int, val green: Int, val blue: Int) : IColor{
	constructor(rgb: Int) : this(rgb, rgb, rgb)
	
	override fun toRGB(): Color{
		return Color(red, green, blue)
	}
}

/**
 * Represents a CIELCH color (http://hclwizard.org/hclwizard).
 *
 * @param[hue] value between 0-360
 * @param[chroma] value between 0-100
 * @param[luminance] value between 0-100
 */
data class HCL(val hue: Double, val chroma: Int, val luminance: Int) : IColor{
	companion object{
		private const val X_NORMALIZED =  95.047
		private const val Y_NORMALIZED = 100.000
		private const val Z_NORMALIZED = 108.883
		
		private const val U_NORMALIZED = (4 * X_NORMALIZED) / (X_NORMALIZED + 15 * Y_NORMALIZED + 3 * Z_NORMALIZED)
		private const val V_NORMALIZED = (9 * Y_NORMALIZED) / (X_NORMALIZED + 15 * Y_NORMALIZED + 3 * Z_NORMALIZED)
	}
	
	override fun toRGB(): Color{
		val hueRad = Math.toRadians(hue)
		
		val l = luminance
		val u = chroma * cos(hueRad)
		val v = chroma * sin(hueRad)
		
		if (luminance == 0){
			return Color(0, 0, 0)
		}
		
		val uu = (u / (13.0 * l)) + U_NORMALIZED
		val vv = (v / (13.0 * l)) + V_NORMALIZED
		
		val y = 0.01 * if (l <= 8)
			Y_NORMALIZED * l * (3.0 / 29.0).pow(3)
		else
			Y_NORMALIZED * ((l + 16) / 116.0).pow(3)
		
		val x = y * (9 * uu) / (4 * vv)
		val z = y * (12 - 3 * uu - 20 * vv) / (4 * vv)
		
		fun fRGB(c: Double) = if (c <= 0.0031308)
			c * 12.92
		else
			(1.055 * c.pow(1.0 / 2.4)) - 0.055
		
		return Color(
			(fRGB( 3.2404542 * x - 1.5371385 * y - 0.4985314 * z) * 255).roundToInt().coerceIn(0, 255),
			(fRGB(-0.9692660 * x + 1.8760108 * y + 0.0415560 * z) * 255).roundToInt().coerceIn(0, 255),
			(fRGB( 0.0556434 * x - 0.2040259 * y + 1.0572252 * z) * 255).roundToInt().coerceIn(0, 255)
		)
	}
}
