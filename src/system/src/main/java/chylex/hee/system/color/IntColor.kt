package chylex.hee.system.color

import chylex.hee.system.math.Vec
import chylex.hee.system.math.floorToInt

inline class IntColor(val i: Int) {
	inline val alpha
		get() = (i ushr 24) and 255
	
	inline val red
		get() = (i ushr 16) and 255
	
	inline val green
		get() = (i ushr 8) and 255
	
	inline val blue
		get() = i and 255
	
	// floats
	
	val alphaF
		get() = alpha / 255F
	
	val redF
		get() = red / 255F
	
	val greenF
		get() = green / 255F
	
	val blueF
		get() = blue / 255F
	
	// destructuring
	
	operator fun component1(): Int = red
	operator fun component2(): Int = green
	operator fun component3(): Int = blue
	operator fun component4(): Int = alpha
	
	// modification
	
	val withNoAlpha
		get() = IntColor(i and (255 shl 24).inv())
	
	fun withAlpha(alpha: Int) =
		IntColor(withNoAlpha.i or (c(alpha) shl 24))
	
	fun withAlpha(alpha: Float) =
		withAlpha((alpha * 255F).floorToInt())
	
	// conversion
	
	val asVec
		get() = Vec(red / 255.0, green / 255.0, blue / 255.0)
	
	val asHSB
		get() = HSB.fromRGB(this)
	
	// construction
	
	@Suppress("FunctionName")
	companion object {
		private fun c(component: Int) = component.coerceIn(0, 255)
		
		
		fun RGB(red: Int, green: Int, blue: Int) =
			IntColor(c(blue) or (c(green) shl 8) or (c(red) shl 16))
		
		fun RGB(rgb: UByte) =
			rgb.toInt().let { RGB(it, it, it) }
		
		
		fun RGBA(red: Int, green: Int, blue: Int, alpha: Int) =
			IntColor(c(blue) or (c(green) shl 8) or (c(red) shl 16) or (c(alpha) shl 24))
		
		fun RGBA(rgb: UByte, alpha: Int) =
			rgb.toInt().let { RGBA(it, it, it, alpha) }
		
		fun RGBA(rgb: UByte, alpha: Float) =
			rgb.toInt().let { RGBA(it, it, it, (alpha * 255F).floorToInt()) }
		
		
		fun HCL(hue: Double, chroma: Float, luminance: Float) =
			chylex.hee.system.color.HCL(hue, chroma, luminance).toRGB()
		
		
		fun HSB(hue: Float, saturation: Float, brightness: Float) =
			chylex.hee.system.color.HSB(hue, saturation, brightness).toRGB()
	}
}
