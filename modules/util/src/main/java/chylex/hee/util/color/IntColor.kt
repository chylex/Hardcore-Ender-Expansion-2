package chylex.hee.util.color

import chylex.hee.util.color.space.HSB
import chylex.hee.util.math.Vec
import chylex.hee.util.math.floorToInt
import java.util.Random

@JvmInline
value class IntColor(val i: Int) : IColorGenerator {
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
		IntColor(withNoAlpha.i or (alpha.coerceIn(0, 255) shl 24))
	
	fun withAlpha(alpha: Float) =
		withAlpha((alpha * 255F).floorToInt())
	
	// conversion
	
	val asVec
		get() = Vec(red / 255.0, green / 255.0, blue / 255.0)
	
	val asHSB
		get() = HSB.fromRGB(this)
	
	// interfaces
	
	override fun next(rand: Random): IntColor {
		return this
	}
}
