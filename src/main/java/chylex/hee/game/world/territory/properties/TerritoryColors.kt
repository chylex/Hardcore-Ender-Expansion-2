package chylex.hee.game.world.territory.properties

import chylex.hee.system.color.IntColor
import java.util.Random

abstract class TerritoryColors {
	abstract val tokenTop: IntColor
	abstract val tokenBottom: IntColor
	
	open val dryVines: Int? = null
	
	abstract val portalSeed: Long
	abstract fun nextPortalColor(rand: Random, color: FloatArray)
}
