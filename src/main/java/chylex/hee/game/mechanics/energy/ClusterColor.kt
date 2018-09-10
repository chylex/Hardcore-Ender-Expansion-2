package chylex.hee.game.mechanics.energy
import chylex.hee.game.render.util.HCL
import chylex.hee.game.render.util.IColor

class ClusterColor(val primaryHue: Short, val secondaryHue: Short){
	val forReceptacle: IColor
		get() = HCL(primaryHue.toDouble(), 75, 80)
}
