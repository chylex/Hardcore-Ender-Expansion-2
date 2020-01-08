package chylex.hee.system.migration.forge
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

typealias Sided = OnlyIn
typealias Side = Dist

typealias SubscribeAllEvents = EventBusSubscriber
typealias SubscribeEvent = SubscribeEvent
typealias EventPriority = EventPriority
typealias EventResult = Event.Result
