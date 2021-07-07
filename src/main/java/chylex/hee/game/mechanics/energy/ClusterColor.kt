package chylex.hee.game.mechanics.energy

import chylex.hee.util.color.HCL
import java.util.Random

class ClusterColor(val primaryHue: Short, val secondaryHue: Short) {
	fun primary(chroma: Float, luminance: Float) = HCL(primaryHue.toDouble(), chroma, luminance)
	fun secondary(chroma: Float, luminance: Float) = HCL(secondaryHue.toDouble(), chroma, luminance)
	
	companion object {
		fun generate(rand: Random): ClusterColor {
			val primary = rand.nextInt(360)
			val secondary = (primary + 30 + rand.nextInt(300)) % 360
			return ClusterColor(primary.toShort(), secondary.toShort())
		}
	}
}
