package chylex.hee.game.block.interfaces

import net.minecraft.block.Block

interface IBlockWithInterfaces {
	fun getInterface(type: Class<out IBlockInterface>): Any?
}

inline fun <reified T : IBlockInterface> IBlockWithInterfaces.getHeeInterface(): T? {
	return this.getInterface(T::class.java) as? T
}

inline fun <reified T : IBlockInterface> Block.getHeeInterface(): T? {
	return (this as? IBlockWithInterfaces)?.getHeeInterface()
}
