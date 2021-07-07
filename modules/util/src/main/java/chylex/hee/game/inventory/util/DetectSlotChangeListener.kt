package chylex.hee.game.inventory.util

import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.IContainerListener
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

class DetectSlotChangeListener(private val container: Container) : IContainerListener {
	private var lastModifiedSlot = -1
	
	override fun sendSlotContents(container: Container, slot: Int, stack: ItemStack) {
		lastModifiedSlot = slot
	}
	
	override fun sendAllContents(container: Container, items: NonNullList<ItemStack>) {}
	override fun sendWindowProperty(container: Container, variable: Int, value: Int) {}
	
	/**
	 * Temporarily adds itself into the list of [container]'s listeners, and returns true if it reported a slot modification while executing the [block].
	 */
	fun run(block: () -> Unit): Int? {
		lastModifiedSlot = -1
		container.listeners.add(this)
		block()
		container.listeners.remove(this)
		return lastModifiedSlot.takeIf { it != -1 }
	}
}
