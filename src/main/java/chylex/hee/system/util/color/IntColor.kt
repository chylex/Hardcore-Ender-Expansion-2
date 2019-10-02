package chylex.hee.system.util.color
import chylex.hee.system.util.floorToInt
import net.minecraft.util.math.Vec3d

inline class IntColor(val i: Int){
	inline val alpha
		get() = (i ushr 24) and 255
	
	inline val red
		get() = (i ushr 16) and 255
	
	inline val green
		get() = (i ushr 8) and 255
	
	inline val blue
		get() = i and 255
	
	// modification
	
	val withNoAlpha
		get() = IntColor(i and (255 shl 24).inv())
	
	fun withAlpha(alpha: Int) =
		IntColor(withNoAlpha.i or (c(alpha) shl 24))
	
	fun withAlpha(alpha: Float) =
		withAlpha((alpha * 255F).floorToInt())
	
	// conversion
	
	val asVec
		get() = Vec3d(red / 255.0, green / 255.0, blue / 255.0)
	
	val asHSB
		get() = FloatArray(3).also { java.awt.Color.RGBtoHSB(red, green, blue, it) }
	
	// construction
	
	@Suppress("FunctionName")
	companion object{
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
			chylex.hee.system.util.color.HCL(hue, chroma, luminance).toRGB()
		
		
		fun HSB(hsb: FloatArray) =
			IntColor(java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]))
	}
}
