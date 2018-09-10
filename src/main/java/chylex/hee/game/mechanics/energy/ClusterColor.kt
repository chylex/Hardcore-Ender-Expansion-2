package chylex.hee.game.mechanics.energy
import chylex.hee.game.render.util.HCL
import chylex.hee.game.render.util.IColor
import java.util.Random

class ClusterColor(val primaryHue: Short, val secondaryHue: Short){
	val forReceptacle: IColor
		get() = HCL(primaryHue.toDouble(), 75, 80)
	
	companion object{
		fun generate(rand: Random): ClusterColor{
			val primary = rand.nextInt(360)
			val secondary = (primary + 30 + rand.nextInt(300)) % 360
			return ClusterColor(primary.toShort(), secondary.toShort())
		}
	}
}
