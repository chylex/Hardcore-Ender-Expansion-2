package chylex.hee.game.territory.system.properties

import chylex.hee.util.color.IntColor
import java.util.Random

abstract class TerritoryColors {
	abstract val tokenTop: IntColor
	abstract val tokenBottom: IntColor
	
	open val dryVines: Int? = null
	
	abstract val portalSeed: Long
	abstract fun nextPortalColor(rand: Random, color: FloatArray)
}
