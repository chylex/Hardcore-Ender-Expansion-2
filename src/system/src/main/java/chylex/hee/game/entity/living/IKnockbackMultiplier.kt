package chylex.hee.game.entity.living
import chylex.hee.HEE
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import net.minecraftforge.event.entity.living.LivingKnockBackEvent

interface IKnockbackMultiplier{
	val lastHitKnockbackMultiplier: Float
	
	@SubscribeAllEvents(modid = HEE.ID)
	object EventHandler{
		@SubscribeEvent(EventPriority.HIGHEST)
		fun onCriticalHit(e: LivingKnockBackEvent){
			val mp = (null /*UPDATE*/ as? IKnockbackMultiplier)?.lastHitKnockbackMultiplier ?: return
			
			if (mp == 0F){
				e.strength = 0F
				e.isCanceled = true
			}
			else{
				e.strength *= mp
			}
		}
	}
}
