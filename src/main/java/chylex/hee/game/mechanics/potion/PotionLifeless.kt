package chylex.hee.game.mechanics.potion
import chylex.hee.HEE
import chylex.hee.game.block.fluid.FluidEnderGoo
import net.minecraftforge.event.entity.living.LivingHealEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HEE.ID)
object PotionLifeless : PotionBase(color = FluidEnderGoo.rgbColor, isNegative = true){
	val LIFELESS = this
	
	override val iconX = 0
	override val iconY = 0
	
	@JvmStatic
	@SubscribeEvent(priority = LOWEST)
	fun onLivingHeal(e: LivingHealEvent){
		if (e.entityLiving.isPotionActive(this)){
			e.isCanceled = true
		}
	}
	
	// TODO maybe render a custom heart texture over empty hearts in the HUD
}
