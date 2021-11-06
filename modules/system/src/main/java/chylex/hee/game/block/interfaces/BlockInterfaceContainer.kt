package chylex.hee.game.block.interfaces

internal class BlockInterfaceContainer(interfaces: Map<Class<out IBlockInterface>, Any>) : IBlockWithInterfaces {
	private val interfaces = interfaces.toMap()
	
	override fun getInterface(type: Class<out IBlockInterface>): Any? {
		return interfaces[type]
	}
}
