package chylex.hee.system

import chylex.hee.HEE
import net.minecraft.item.Item
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.EventBus
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Works around event bus issue where registering an Item object crashes dedicated servers.
 * Does not process parent classes or interfaces.
 */
object MinecraftForgeEventBus {
	fun register(item: Item) {
		val registerListener = EventBus::class.java.getDeclaredMethod("registerListener", Any::class.java, Method::class.java, Method::class.java).also {
			it.isAccessible = true
		}
		
		for (listener in item.javaClass.methods.filter { !Modifier.isStatic(it.modifiers) && it.isAnnotationPresent(SubscribeEvent::class.java) }) {
			if (HEE.debug) {
				HEE.log.info("[MinecraftForgeEventBus] registering ${listener.parameterTypes.firstOrNull()?.name?.substringAfterLast('.')} for ${item.javaClass.simpleName}")
			}
			
			registerListener.invoke(MinecraftForge.EVENT_BUS, item, listener, listener)
		}
	}
}
