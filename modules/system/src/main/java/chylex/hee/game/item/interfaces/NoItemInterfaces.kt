package chylex.hee.game.item.interfaces

internal object NoItemInterfaces : IItemWithInterfaces {
	override fun getInterface(type: Class<out IItemInterface>): Any? {
		return null
	}
}
