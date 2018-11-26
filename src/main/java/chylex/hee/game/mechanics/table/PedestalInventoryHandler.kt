package chylex.hee.game.mechanics.table
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.gui.util.InvReverseWrapper
import chylex.hee.system.util.createSnapshot
import chylex.hee.system.util.getStack
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.loadInventory
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.restoreSnapshot
import chylex.hee.system.util.saveInventory
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.setStack
import chylex.hee.system.util.size
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.InventoryBasic
import net.minecraft.inventory.InventoryHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

class PedestalInventoryHandler(private val updateCallback: (Boolean) -> Unit) : INBTSerializable<NBTTagCompound>{
	var itemInput: ItemStack = ItemStack.EMPTY
		private set
	
	private val itemOutput = InventoryBasic("[Output]", false, 9).apply {
		addInventoryChangeListener { onInventoryUpdated(updateInputModCounter = false) }
	}
	
	val itemOutputCap = InvReverseWrapper(itemOutput)
	
	val hasOutput
		get() = itemOutput.nonEmptySlots.hasNext()
	
	val nonEmptyStacks
		get() = ArrayList<ItemStack>(10).apply {
			if (itemInput.isNotEmpty){
				add(itemInput)
			}
			
			for((_, stack) in itemOutput.nonEmptySlots){
				add(stack)
			}
		}
	
	private var pauseInventoryUpdates = false
	
	// Behavior
	
	fun addToInput(stack: ItemStack): Boolean{
		if (stack.isEmpty){
			return false
		}
		
		var success = false
		
		if (itemInput.isEmpty){
			itemInput = stack.copy()
			stack.size = 0
			success = true
		}
		else if (ItemHandlerHelper.canItemStacksStack(stack, itemInput)){
			val movedAmount = min(itemInput.maxStackSize - itemInput.size, stack.size)
			
			if (movedAmount > 0){
				itemInput.size += movedAmount
				stack.size -= movedAmount
				success = true
			}
		}
		
		if (!success){
			return false
		}
		
		onInventoryUpdated(updateInputModCounter = true)
		return true
	}
	
	fun addToOutput(stacks: Array<ItemStack>): Boolean{
		pauseInventoryUpdates = true
		
		val prevOutput = itemOutput.createSnapshot()
		val hasStoredEverything = stacks.all { itemOutput.addItem(it).isEmpty }
		
		if (!hasStoredEverything){
			itemOutput.restoreSnapshot(prevOutput)
		}
		
		pauseInventoryUpdates = false
		
		if (hasStoredEverything){
			itemInput = ItemStack.EMPTY
			onInventoryUpdated(updateInputModCounter = true)
			return true
		}
		
		return false
	}
	
	fun replaceInputSilently(newInput: ItemStack): Boolean{
		if (ItemStack.areItemStacksEqual(itemInput, newInput)){
			return false
		}
		
		itemInput = newInput.copy()
		onInventoryUpdated(updateInputModCounter = false)
		return true
	}
	
	fun moveOutputToPlayerInventory(inventory: InventoryPlayer): Boolean{
		var hasTransferedAnything = false
		
		for((_, stack) in itemOutput.nonEmptySlots){
			val prevStackSize = stack.size
			
			if (inventory.addItemStackToInventory(stack) || stack.size != prevStackSize){ // addItemStackToInventory returns false if combined w/ existing slot
				hasTransferedAnything = true
			}
		}
		
		if (!hasTransferedAnything){
			return false
		}
		
		onInventoryUpdated(updateInputModCounter = false)
		return true
	}
	
	fun dropAllItems(world: World, pos: BlockPos){
		val posAbove = pos.up()
		val itemArea = AxisAlignedBB(posAbove)
		val previousItemEntities = world.selectExistingEntities.inBox<EntityItem>(itemArea).toSet()
		
		// UPDATE: see if 1.13 fixes itemstacks spawning and spazzing out all over the fucking place
		InventoryHelper.spawnItemStack(world, posAbove.x.toDouble(), posAbove.y.toDouble(), posAbove.z.toDouble(), itemInput)
		InventoryHelper.dropInventoryItems(world, posAbove, itemOutput)
		
		for(itemEntity in world.selectExistingEntities.inBox<EntityItem>(itemArea)){
			if (!previousItemEntities.contains(itemEntity)){
				itemEntity.setNoPickupDelay()
				itemEntity.motionVec = Vec3d.ZERO
				itemEntity.thrower = BlockTablePedestal.DROPPED_ITEM_THROWER_NAME
			}
		}
		
		itemInput = ItemStack.EMPTY
		itemOutput.clear()
		onInventoryUpdated(updateInputModCounter = true)
	}
	
	private fun onInventoryUpdated(updateInputModCounter: Boolean){
		if (!pauseInventoryUpdates){
			updateCallback(updateInputModCounter)
		}
	}
	
	// Serialization
	
	override fun serializeNBT() = NBTTagCompound().apply {
		setStack("Input", itemInput)
		saveInventory("Output", itemOutput)
	}
	
	override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
		itemInput = getStack("Input")
		loadInventory("Output", itemOutput)
	}
}
