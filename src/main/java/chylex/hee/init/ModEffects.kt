package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.game.potion.BanishmentEffect
import chylex.hee.game.potion.CorruptionEffect
import chylex.hee.game.potion.LifelessEffect
import chylex.hee.game.potion.PurityEffect
import chylex.hee.system.named
import chylex.hee.system.registerAllFields
import chylex.hee.util.forge.SubscribeAllEvents
import net.minecraft.potion.Effect
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModEffects {
	@JvmField val LIFELESS   = LifelessEffect named "lifeless"
	@JvmField val PURITY     = PurityEffect named "purity"
	@JvmField val CORRUPTION = CorruptionEffect named "corruption"
	@JvmField val BANISHMENT = BanishmentEffect named "banishment"
	
	@SubscribeEvent
	fun onRegisterEffects(e: RegistryEvent.Register<Effect>) {
		e.registerAllFields(this)
	}
}
