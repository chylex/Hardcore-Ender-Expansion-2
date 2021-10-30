package chylex.hee.game.item.interfaces

internal class ItemInterfaceContainer(interfaces: Map<Class<out IItemInterface>, Any>) : IItemWithInterfaces {
	private val interfaces = interfaces.toMap()
	
	override fun getInterface(type: Class<out IItemInterface>): Any? {
		return interfaces[type]
	}
}
