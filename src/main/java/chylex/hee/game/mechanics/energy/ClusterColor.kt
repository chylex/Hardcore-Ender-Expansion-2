package chylex.hee.game.mechanics.energy
import chylex.hee.render.util.HCL
import chylex.hee.render.util.IColor

class ClusterColor(val primaryHue: Short, val secondaryHue: Short){
	val forReceptacle: IColor
		get() = HCL(primaryHue.toDouble(), 75, 80)
}
