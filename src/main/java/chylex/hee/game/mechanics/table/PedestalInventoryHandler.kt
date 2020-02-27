package chylex.hee.game.mechanics.table
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.container.util.InvReverseWrapper
import chylex.hee.init.ModItems
import chylex.hee.system.migration.vanilla.EntityItem
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.copyIf
import chylex.hee.system.util.createSnapshot
import chylex.hee.system.util.getStack
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.loadInventory
import chylex.hee.system.util.mergeStackProperly
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextVector2
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.putStack
import chylex.hee.system.util.restoreSnapshot
import chylex.hee.system.util.saveInventory
import chylex.hee.system.util.size
import chylex.hee.system.util.use
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.Container
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

class PedestalInventoryHandler(private val updateCallback: (Boolean) -> Unit) : INBTSerializable<TagCompound>{
	private companion object{
		private const val INPUT_TAG = "Input"
		private const val OUTPUT_TAG = "Output"
	}
	
	var itemInput: ItemStack = ItemStack.EMPTY
		private set
	
	private val itemOutput = Inventory(9).apply {
		addListener { onInventoryUpdated(updateInputModCounter = false) }
	}
	
	val itemOutputCap = InvReverseWrapper(itemOutput)
	
	val hasOutput
		get() = itemOutput.nonEmptySlots.hasNext()
	
	val outputComparatorStrength
		get() = Container.calcRedstoneFromInventory(itemOutput)
	
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
		val hasStoredEverything = stacks.all { it.copy().apply(::tryMergeIntoOutput).isEmpty }
		
		if (!hasStoredEverything){
			itemOutput.restoreSnapshot(prevOutput)
		}
		
		pauseInventoryUpdates = false
		
		if (hasStoredEverything){
			onInventoryUpdated(updateInputModCounter = false)
			return true
		}
		
		return false
	}
	
	private fun tryMergeIntoOutput(merging: ItemStack){
		if (merging.item === ModItems.EXPERIENCE_BOTTLE){
			val bottle = ModItems.EXPERIENCE_BOTTLE
			
			for((_, stack) in itemOutput.nonEmptySlots){
				if (stack.item === bottle){
					while(bottle.mergeBottles(merging, stack) && bottle.isFullOfExperience(stack)){
						val moved = stack.copy().also { it.size = 1 }
						
						stack.shrink(1)
						itemOutput.mergeStackProperly(moved)
					}
					
					if (merging.isEmpty){
						return
					}
				}
			}
		}
		
		if (merging.isNotEmpty){
			itemOutput.mergeStackProperly(merging)
		}
	}
	
	fun replaceInput(newInput: ItemStack, silent: Boolean): Boolean{
		if (ItemStack.areItemStacksEqual(itemInput, newInput)){
			return false
		}
		
		itemInput = newInput.copyIf { it.isNotEmpty }
		onInventoryUpdated(updateInputModCounter = !silent)
		return true
	}
	
	fun moveOutputToPlayerInventory(inventory: PlayerInventory): Boolean{
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
	
	fun dropInputItem(world: World, pos: BlockPos){
		if (itemInput.isEmpty){
			return
		}
		
		spawnItem(world, pos, itemInput, addOffset = false)
		itemInput = ItemStack.EMPTY
		
		onInventoryUpdated(updateInputModCounter = true)
	}
	
	fun dropAllItems(world: World, pos: BlockPos){
		spawnItem(world, pos, itemInput, addOffset = false)
		itemInput = ItemStack.EMPTY
		
		itemOutput.nonEmptySlots.forEach { spawnItem(world, pos, it.stack, addOffset = true) }
		itemOutput.clear()
		
		onInventoryUpdated(updateInputModCounter = true)
	}
	
	private fun spawnItem(world: World, pos: BlockPos, stack: ItemStack, addOffset: Boolean){
		val rand = world.rand
		
		val offsetX: Double
		val offsetZ: Double
		val motionXZ: Double
		
		if (addOffset){
			offsetX = rand.nextFloat(0.22, 0.78)
			offsetZ = rand.nextFloat(0.22, 0.78)
			motionXZ = rand.nextFloat(0.005, 0.007)
		}
		else{
			offsetX = 0.5
			offsetZ = 0.5
			motionXZ = rand.nextFloat(0.032, 0.034)
		}
		
		EntityItem(world, pos.x + offsetX, pos.y - 1.0 + BlockTablePedestal.ITEM_SPAWN_OFFSET_Y, pos.z + offsetZ, stack).apply {
			setNoPickupDelay()
			motion = rand.nextVector2(motionXZ, rand.nextFloat(0.152, 0.155))
			throwerId = BlockTablePedestal.DROPPED_ITEM_THROWER
			world.addEntity(this)
		}
	}
	
	private fun onInventoryUpdated(updateInputModCounter: Boolean){
		if (!pauseInventoryUpdates){
			updateCallback(updateInputModCounter)
		}
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		putStack(INPUT_TAG, itemInput)
		saveInventory(OUTPUT_TAG, itemOutput)
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		itemInput = getStack(INPUT_TAG)
		loadInventory(OUTPUT_TAG, itemOutput)
	}
}
