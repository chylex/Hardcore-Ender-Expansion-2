package chylex.hee.game.mechanics.instability.region.components
import chylex.hee.game.entity.living.EntityMobEndermiteInstability
import chylex.hee.game.world.util.Teleporter
import chylex.hee.system.util.Pos
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextVector
import chylex.hee.system.util.posVec
import chylex.hee.system.util.removeItem
import net.minecraft.util.math.Vec3d
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
		val world = endermite.world
		val originalPos = endermite.posVec
		val originalBox = endermite.entityBoundingBox
		
		repeat(64){
			val randomPos = originalPos.add(rand.nextVector(rand.nextFloat(3.0, 6.0)))
			val newPos = Vec3d(randomPos.x, randomPos.y.floorToInt() + 0.01, randomPos.z)
			
			if (Pos(newPos).down().blocksMovement(world) && world.getCollisionBoxes(endermite, originalBox.offset(newPos.subtract(originalPos))).isEmpty()){
				TELEPORT.toLocation(endermite, newPos)
				return
			}
		}
	}
}
