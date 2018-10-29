package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.system.util.getStack
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.loadInventory
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.saveInventory
import chylex.hee.system.util.setStack
import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

class TileEntityTablePedestal : TileEntityBase(){
	
	// Properties (Inventory)
	
	private var itemInput = ItemStack.EMPTY
	private val itemOutput = InventoryBasic("[Output]", false, 9).apply { addInventoryChangeListener { onInventoryUpdated() } }
	
	private val hasNoItems
		get() = itemInput.isEmpty && !itemOutput.nonEmptySlots.hasNext()
	
	var stacksForRendering = emptyArray<ItemStack>()
		private set
	
	// Behavior (Inventory)
	
	private fun onInventoryUpdated(){
		// TODO
	}
	
	// Serialization
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		setStack("Input", itemInput)
		saveInventory("Output", itemOutput)
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		itemInput = getStack("Input")
		loadInventory("Output", itemOutput)
		
		if (context == NETWORK){
			val nonEmptyStacks = ArrayList<ItemStack>(10).apply {
				if (itemInput.isNotEmpty){
					add(itemInput)
				}
				
				for((_, stack) in itemOutput.nonEmptySlots){
					add(stack)
				}
			}
			
			stacksForRendering = nonEmptyStacks.toTypedArray()
		}
	}
}
