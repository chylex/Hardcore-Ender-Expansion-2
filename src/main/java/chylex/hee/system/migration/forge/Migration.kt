package chylex.hee.system.migration.forge
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

typealias Sided = SideOnly
typealias Side = Side

typealias SubscribeAllEvents = EventBusSubscriber
typealias SubscribeEvent = SubscribeEvent
typealias EventPriority = EventPriority
typealias EventResult = Event.Result
