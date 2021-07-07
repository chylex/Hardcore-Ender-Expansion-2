package chylex.hee.game.entity.living

import chylex.hee.HEE
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.EventResult
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraftforge.event.entity.player.CriticalHitEvent

interface ICritTracker {
	var wasLastHitCritical: Boolean
	
	@SubscribeAllEvents(modid = HEE.ID)
	object EventHandler {
		@SubscribeEvent(EventPriority.LOWEST, receiveCanceled = true)
		fun onCriticalHit(e: CriticalHitEvent) {
			(e.target as? ICritTracker)?.wasLastHitCritical = e.result == EventResult.ALLOW || (e.result == EventResult.DEFAULT && e.isVanillaCritical)
		}
	}
}
