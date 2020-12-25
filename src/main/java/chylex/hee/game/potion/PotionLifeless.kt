package chylex.hee.game.potion

import chylex.hee.HEE
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.migration.Potion
import net.minecraft.potion.EffectType.HARMFUL
import net.minecraftforge.event.entity.living.LivingHealEvent

@SubscribeAllEvents(modid = HEE.ID)
object PotionLifeless : Potion(HARMFUL, FluidEnderGoo.rgbColor.i) {
	@SubscribeEvent(EventPriority.LOWEST)
	fun onLivingHeal(e: LivingHealEvent) {
		if (e.entityLiving.isPotionActive(this)) {
			e.isCanceled = true
		}
	}
	
	// POLISH maybe render a custom heart texture over empty hearts in the HUD
}
