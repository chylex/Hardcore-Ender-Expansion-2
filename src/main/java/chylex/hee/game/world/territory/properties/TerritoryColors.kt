package chylex.hee.game.world.territory.properties
import chylex.hee.system.util.color.IColor
import java.util.Random

abstract class TerritoryColors{
	abstract val tokenTop: IColor
	abstract val tokenBottom: IColor
	
	abstract val portalSeed: Long
	abstract fun nextPortalColor(rand: Random, color: FloatArray)
}
