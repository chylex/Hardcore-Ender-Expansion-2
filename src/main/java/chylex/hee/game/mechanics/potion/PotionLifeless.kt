package chylex.hee.game.mechanics.potion
import chylex.hee.HEE
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraft.potion.EffectType.HARMFUL
import net.minecraftforge.event.entity.living.LivingHealEvent

@SubscribeAllEvents(modid = HEE.ID)
object PotionLifeless : PotionBase(color = FluidEnderGoo.rgbColor, kind = HARMFUL){
	@SubscribeEvent(EventPriority.LOWEST)
	fun onLivingHeal(e: LivingHealEvent){
		if (e.entityLiving.isPotionActive(this)){
			e.isCanceled = true
		}
	}
	
	// TODO maybe render a custom heart texture over empty hearts in the HUD
}
