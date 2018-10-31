package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.gui.util.InvReverseWrapper
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.getStack
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.loadInventory
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nonEmptySlots
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
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

class TileEntityTablePedestal : TileEntityBase(){
	
	// Properties (Inventory)
	
	private var itemInput = ItemStack.EMPTY
	private val itemOutput = InventoryBasic("[Output]", false, 9).apply { addInventoryChangeListener { onInventoryUpdated() } }
	private val itemOutputCap = InvReverseWrapper(itemOutput)
	
	private val hasNoItems
		get() = itemInput.isEmpty && !itemOutput.nonEmptySlots.hasNext()
	
	var stacksForRendering = emptyArray<ItemStack>()
		private set
	
	// Behavior (Inventory)
	
	fun addToInput(stack: ItemStack): Boolean{
		if (stack.isEmpty || BlockTablePedestal.isItemAreaBlocked(world, pos)){
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
		
		onInventoryUpdated()
		return true
	}
	
	fun moveOutputToPlayerInventory(inventory: InventoryPlayer): Boolean{
		if (BlockTablePedestal.isItemAreaBlocked(world, pos)){
			return false
		}
		
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
		
		onInventoryUpdated()
		return true
	}
	
	fun dropAllItems(){
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
				itemEntity.thrower = "[Pedestal]"
			}
		}
		
		itemInput = ItemStack.EMPTY
		itemOutput.clear()
		onInventoryUpdated()
	}
	
	private fun onInventoryUpdated(){
		notifyUpdate(FLAG_SYNC_CLIENT or FLAG_MARK_DIRTY)
	}
	
	override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean{
		return (capability === ITEM_HANDLER_CAPABILITY && facing == DOWN) || super.hasCapability(capability, facing)
	}
	
	override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T?{
		return if (capability === ITEM_HANDLER_CAPABILITY && facing == DOWN)
			ITEM_HANDLER_CAPABILITY.cast(itemOutputCap)
		else
			super.getCapability(capability, facing)
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
