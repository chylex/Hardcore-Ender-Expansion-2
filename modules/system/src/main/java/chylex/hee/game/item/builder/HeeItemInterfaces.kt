package chylex.hee.game.item.builder

import chylex.hee.game.item.interfaces.IItemInterface
import chylex.hee.game.item.interfaces.ItemInterfaceContainer
import chylex.hee.game.item.interfaces.NoItemInterfaces

class HeeItemInterfaces {
	private val interfaces = mutableMapOf<Class<out IItemInterface>, Any>()
	
	internal val delegate
		get() = if (interfaces.isEmpty())
			NoItemInterfaces
		else
			ItemInterfaceContainer(interfaces)
	
	operator fun <T : IItemInterface> set(type: Class<T>, impl: T) {
		interfaces[type] = impl
	}
	
	fun includeFrom(source: HeeItemInterfaces) {
		this.interfaces.putAll(source.interfaces)
	}
}
