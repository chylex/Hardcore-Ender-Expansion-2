package chylex.hee.game.mechanics.instability.region.components
import chylex.hee.game.entity.living.EntityMobEndermiteInstability
import chylex.hee.game.world.util.Teleporter
import chylex.hee.system.util.posVec
import chylex.hee.system.util.removeItem
import java.util.Random
import kotlin.math.min

internal object EndermiteTeleportLogic{
	private val TELEPORT = Teleporter(resetPathfinding = false, damageDealt = 1F, extendedEffectRange = 48F)
	
	fun call(endermites: List<EntityMobEndermiteInstability>, amount: Int, rand: Random): Int{
		val remaining = endermites.toMutableList()
		var totalTeleported = 0
		
		repeat(min(endermites.size, amount)){
			performTeleportation(rand.removeItem(remaining)!!, rand)
			++totalTeleported
		}
		
		return totalTeleported
	}
	
	private fun performTeleportation(endermite: EntityMobEndermiteInstability, rand: Random){
		TELEPORT.nearLocation(endermite, rand, endermite.posVec, distance = (3.0)..(6.0), attempts = 64)
	}
}
