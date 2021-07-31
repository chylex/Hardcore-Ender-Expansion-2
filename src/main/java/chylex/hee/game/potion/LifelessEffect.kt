package chylex.hee.game.potion

import chylex.hee.HEE
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraft.potion.EffectType.HARMFUL
import net.minecraftforge.event.entity.living.LivingHealEvent

@SubscribeAllEvents(modid = HEE.ID)
object LifelessEffect : HeeEffect(HARMFUL, FluidEnderGoo.rgbColor) {
	@SubscribeEvent(EventPriority.LOWEST)
	fun onLivingHeal(e: LivingHealEvent) {
		if (e.entityLiving.isPotionActive(this)) {
			e.isCanceled = true
		}
	}
	
	// POLISH maybe render a custom heart texture over empty hearts in the HUD
}
