package chylex.hee.game.mechanics.portal

import net.minecraft.entity.Entity

object EntityPortalContact {
	private const val DELAY = 10
	
	fun shouldTeleport(entity: Entity): Boolean {
		if (entity.world.isRemote) {
			return false
		}
		
		if (entity.portalCooldown == 0 && entity.ticksExisted > 1) {
			entity.portalCooldown = DELAY
			return true
		}
		
		if (entity.portalCooldown < DELAY) {
			entity.portalCooldown = DELAY
		}
		
		return false
	}
}
