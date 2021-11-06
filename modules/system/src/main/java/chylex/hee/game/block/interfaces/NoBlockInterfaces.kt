package chylex.hee.game.block.interfaces

internal object NoBlockInterfaces : IBlockWithInterfaces {
	override fun getInterface(type: Class<out IBlockInterface>): Any? {
		return null
	}
}
