package chylex.hee.game.entity.util
import chylex.hee.HEE
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraftforge.event.entity.living.LivingKnockBackEvent

interface IKnockbackMultiplier{
	val lastHitKnockbackMultiplier: Float
	
	@SubscribeAllEvents(modid = HEE.ID)
	object EventHandler{
		@JvmStatic
		@SubscribeEvent(EventPriority.HIGHEST)
		fun onCriticalHit(e: LivingKnockBackEvent){
			val mp = (e.originalAttacker as? IKnockbackMultiplier)?.lastHitKnockbackMultiplier ?: return
			
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
