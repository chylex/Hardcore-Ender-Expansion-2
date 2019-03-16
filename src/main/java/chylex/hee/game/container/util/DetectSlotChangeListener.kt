package chylex.hee.game.container.util
import net.minecraft.inventory.Container
import net.minecraft.inventory.IContainerListener
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

class DetectSlotChangeListener : IContainerListener{
	private var lastModifiedSlot = -1
	
	override fun sendSlotContents(container: Container, slot: Int, stack: ItemStack){
		lastModifiedSlot = slot
	}
	
	override fun sendAllContents(container: Container, items: NonNullList<ItemStack>){}
	override fun sendWindowProperty(container: Container, variable: Int, value: Int){}
	override fun sendAllWindowProperties(container: Container, inventory: IInventory){}
	
	/**
	 * Temporarily adds itself into the list of [Container]'s [listeners], and returns true if it reported a slot modification while executing the [block].
	 */
	fun restart(listeners: MutableList<IContainerListener>, block: () -> Unit): Int?{
		lastModifiedSlot = -1
		listeners.add(this)
		block()
		listeners.remove(this)
		return lastModifiedSlot.takeIf { it != -1 }
	}
}
