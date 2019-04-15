package chylex.hee.game.mechanics.portal
import net.minecraft.entity.Entity
import java.util.UUID

object EntityPortalContact{
	private val waitingForContact = HashSet<UUID>()
	
	fun shouldTeleport(entity: Entity): Boolean{
		if (entity.world.isRemote){
			return false
		}
		
		val id = entity.uniqueID
		
		if (waitingForContact.remove(id)){
			entity.timeUntilPortal = 10
			return false
		}
		else if (entity.timeUntilPortal == 0){
			entity.timeUntilPortal = 10
			waitingForContact.add(id)
			return true
		}
		else{
			entity.timeUntilPortal = 10
			return false
		}
	}
}
