package chylex.hee.game.block.builder

import chylex.hee.game.block.interfaces.BlockInterfaceContainer
import chylex.hee.game.block.interfaces.IBlockInterface
import chylex.hee.game.block.interfaces.NoBlockInterfaces

class HeeBlockInterfaces {
	private val interfaces = mutableMapOf<Class<out IBlockInterface>, Any>()
	
	internal val delegate
		get() = if (interfaces.isEmpty())
			NoBlockInterfaces
		else
			BlockInterfaceContainer(interfaces)
	
	operator fun <T : IBlockInterface> set(type: Class<T>, impl: T) {
		interfaces[type] = impl
	}
	
	fun includeFrom(source: HeeBlockInterfaces) {
		this.interfaces.putAll(source.interfaces)
	}
}
