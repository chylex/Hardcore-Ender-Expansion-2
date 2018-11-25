package chylex.hee.game.entity.util
import chylex.hee.HEE
import net.minecraftforge.event.entity.player.CriticalHitEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW
import net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT
import net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

interface ICritTracker{
	var wasLastHitCritical: Boolean
	
	@EventBusSubscriber(modid = HEE.ID)
	object EventHandler{
		@JvmStatic
		@SubscribeEvent(priority = LOWEST, receiveCanceled = true)
		fun onCriticalHit(e: CriticalHitEvent){
			(e.target as? ICritTracker)?.wasLastHitCritical = e.result == ALLOW || (e.result == DEFAULT && e.isVanillaCritical)
		}
	}
}
