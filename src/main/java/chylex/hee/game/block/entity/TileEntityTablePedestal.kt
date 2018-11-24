package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.BlockTablePedestal.Companion.IS_LINKED
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.gui.util.InvReverseWrapper
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.getStack
import chylex.hee.system.util.getTile
import chylex.hee.system.util.isLoaded
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.loadInventory
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.saveInventory
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.setPos
import chylex.hee.system.util.setStack
import chylex.hee.system.util.size
import chylex.hee.system.util.updateState
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.InventoryBasic
import net.minecraft.inventory.InventoryHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min
import kotlin.properties.Delegates

class TileEntityTablePedestal : TileEntityBase(){
	
	// Properties (Linking)
	
	private var linkedTable: BlockPos? by Delegates.observable<BlockPos?>(null){
		_, oldValue, newValue -> if (oldValue != newValue) onLinkedStatusChanged()
	}
	
	private val linkedTableTile: TileEntityBaseTable<*>?
		get() = linkedTable?.getTile(world)
	
	private val linkedTableTileIfLoaded: TileEntityBaseTable<*>?
		get() = linkedTable?.takeIf { it.isLoaded(world) }?.getTile(world)
	
	val hasLinkedTable
		get() = linkedTable != null
	
	val tableIndicatorColor: Int?
		get() = linkedTableTileIfLoaded?.tableIndicatorColor
	
	// Properties (Inventory)
	
	private var itemInput = ItemStack.EMPTY
	private val itemOutput = InventoryBasic("[Output]", false, 9).apply { addInventoryChangeListener { onInventoryUpdated() } }
	private val itemOutputCap = InvReverseWrapper(itemOutput)
	
	private val hasNoItems
		get() = itemInput.isEmpty && !itemOutput.nonEmptySlots.hasNext()
	
	var stacksForRendering = emptyArray<ItemStack>()
		private set
	
	// Behavior (General)
	
	fun onPedestalDestroyed(dropTableLink: Boolean){
		linkedTableTile?.tryUnlinkPedestal(this, dropTableLink)
		linkedTable = null // must reset state because the method is called twice if the player is in creative mode
		
		dropAllItems()
	}
	
	// Behavior (Linking)
	
	fun setLinkedTable(tile: TileEntityBaseTable<*>){
		val currentlyLinkedTable = linkedTableTile
		
		if (tile !== currentlyLinkedTable){
			currentlyLinkedTable?.tryUnlinkPedestal(this, dropTableLink = false)
			linkedTable = tile.pos
		}
	}
	
	fun resetLinkedTable(dropTableLink: Boolean){
		linkedTableTile?.tryUnlinkPedestal(this, dropTableLink)
	}
	
	fun onTableUnlinked(tile: TileEntityBaseTable<*>, dropTableLink: Boolean){
		unlinkTableInternal(tile, if (dropTableLink) pos.up() else null)
	}
	
	fun onTableDestroyed(tile: TileEntityBaseTable<*>, dropTableLink: Boolean){
		unlinkTableInternal(tile, if (dropTableLink) tile.pos else null)
	}
	
	private fun unlinkTableInternal(tile: TileEntityBaseTable<*>, dropTableLinkAt: BlockPos?){
		linkedTable?.takeIf { it == tile.pos }?.let {
			dropTableLinkAt?.let(::spawnTableLinkAt)
			linkedTable = null
		}
	}
	
	private fun spawnTableLinkAt(pos: BlockPos){
		val rand = world.rand
		
		EntityItem(world, pos.x + rand.nextFloat(0.25, 0.75), pos.y + rand.nextFloat(0.25, 0.75), pos.z + rand.nextFloat(0.25, 0.75), ItemStack(ModItems.TABLE_LINK)).apply {
			setDefaultPickupDelay()
			thrower = BlockTablePedestal.DROPPED_ITEM_THROWER_NAME
			world.spawnEntity(this)
		}
	}
	
	private fun onLinkedStatusChanged(){
		if (world != null){
			pos.updateState(world, ModBlocks.TABLE_PEDESTAL, FLAG_SYNC_CLIENT){ it.withProperty(IS_LINKED, linkedTable != null) }
		}
	}
	
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
				itemEntity.thrower = BlockTablePedestal.DROPPED_ITEM_THROWER_NAME
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
	
	override fun shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState): Boolean{
		return newState.block != oldState.block
	}
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		linkedTable?.let {
			setPos("TablePos", it)
		}
		
		setStack("Input", itemInput)
		saveInventory("Output", itemOutput)
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		linkedTable = getPosOrNull("TablePos")
		
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
