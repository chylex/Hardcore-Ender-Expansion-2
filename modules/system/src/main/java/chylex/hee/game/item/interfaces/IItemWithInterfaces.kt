package chylex.hee.game.item.interfaces

import net.minecraft.item.Item

interface IItemWithInterfaces {
	fun getInterface(type: Class<out IItemInterface>): Any?
}

inline fun <reified T : IItemInterface> IItemWithInterfaces.getHeeInterface(): T? {
	return this.getInterface(T::class.java) as? T
}

inline fun <reified T : IItemInterface> Item.getHeeInterface(): T? {
	return (this as? IItemWithInterfaces)?.getHeeInterface()
}
