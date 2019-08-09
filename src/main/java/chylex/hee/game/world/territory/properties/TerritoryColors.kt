package chylex.hee.game.world.territory.properties
import chylex.hee.system.util.color.IntColor
import java.util.Random

abstract class TerritoryColors{
	abstract val tokenTop: IntColor
	abstract val tokenBottom: IntColor
	
	abstract val portalSeed: Long
	abstract fun nextPortalColor(rand: Random, color: FloatArray)
}
