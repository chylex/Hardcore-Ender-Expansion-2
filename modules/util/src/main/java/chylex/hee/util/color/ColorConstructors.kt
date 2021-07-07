package chylex.hee.util.color

import chylex.hee.util.math.floorToInt

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
	chylex.hee.util.color.space.HCL(hue, chroma, luminance).toRGB()

fun HSB(hue: Float, saturation: Float, brightness: Float) =
	chylex.hee.util.color.space.HSB(hue, saturation, brightness).toRGB()
