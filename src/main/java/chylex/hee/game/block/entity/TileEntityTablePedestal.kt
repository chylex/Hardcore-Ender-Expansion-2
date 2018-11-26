package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.BlockTablePedestal.Companion.IS_LINKED
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.game.mechanics.table.PedestalInventoryHandler
import chylex.hee.game.mechanics.table.PedestalStatusIndicator
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Contents.NONE
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Contents.OUTPUTTED
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Contents.WITH_INPUT
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.DEDICATED_OUTPUT
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.getTile
import chylex.hee.system.util.isLoaded
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.setPos
import chylex.hee.system.util.updateState
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY

class TileEntityTablePedestal : TileEntityBase(){
	
	// Properties (Tables)
	
	private var linkedTable by NotifyOnChange<BlockPos?>(null, ::onLinkedStatusChanged)
	
	private val linkedTableTile: TileEntityBaseTable<*>?
		get() = linkedTable?.getTile(world)
	
	val hasLinkedTable
		get() = linkedTable != null
	
	val tableIndicatorColor: Int?
		get() = linkedTable?.takeIf { it.isLoaded(world) }?.getTile<TileEntityBaseTable<*>>(world)?.tableIndicatorColor
	
	private val statusIndicator = PedestalStatusIndicator(this)
	
	var statusIndicatorColorClient by NotifyOnChange<Int?>(null){
		-> world.markBlockRangeForRenderUpdate(pos, pos)
	}
	
	var isDedicatedOutput
		get() = statusIndicator.process == DEDICATED_OUTPUT
		set(value){
			if (value){
				statusIndicator.process = DEDICATED_OUTPUT
				inventoryHandler.dropInputItem(world, pos.up())
			}
			else{
				statusIndicator.process = null
			}
		}
	
	// Properties (Inventory)
	
	private val inventoryHandler = PedestalInventoryHandler(::onInventoryUpdated)
	
	val hasInputItem
		get() = inventoryHandler.itemInput.isNotEmpty
	
	val itemInputCopy: ItemStack
		get() = inventoryHandler.itemInput.copy()
	
	var inputModTime = 0L
		private set
	
	var inputModCounter = 0
		private set
	
	var stacksForRendering = emptyArray<ItemStack>()
		private set
	
	// Utilities
	
	fun <T> SyncOnChange(initialValue: T) = NotifyOnChange(initialValue){
		-> if (isLoaded) notifyUpdate(FLAG_SYNC_CLIENT or FLAG_MARK_DIRTY)
	}
	
	// Behavior (General)
	
	fun onPedestalDestroyed(dropTableLink: Boolean){
		linkedTableTile?.tryUnlinkPedestal(this, dropTableLink)
		linkedTable = null // must reset state because the method is called twice if the player is in creative mode
		
		dropAllItems()
	}
	
	// Behavior (Tables)
	
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
	
	fun requestMarkAsOutput(): Boolean{
		return linkedTableTile?.tryMarkInputPedestalAsOutput(this) == true
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
			statusIndicator.process = null
		}
	}
	
	fun updateProcessStatus(newStatus: PedestalStatusIndicator.Process?){
		statusIndicator.process = newStatus
	}
	
	// Behavior (Inventory)
	
	fun addToInput(stack: ItemStack): Boolean{
		return !isDedicatedOutput && inventoryHandler.addToInput(stack)
	}
	
	fun addToOutput(stacks: Array<ItemStack>): Boolean{
		return inventoryHandler.addToOutput(stacks)
	}
	
	fun replaceInputSilently(newInput: ItemStack): Boolean{
		return inventoryHandler.replaceInputSilently(newInput)
	}
	
	fun moveOutputToPlayerInventory(inventory: InventoryPlayer): Boolean{
		return inventoryHandler.moveOutputToPlayerInventory(inventory)
	}
	
	fun dropAllItems(){
		inventoryHandler.dropAllItems(world, pos.up())
	}
	
	private fun onInventoryUpdated(updateInputModCounter: Boolean){
		statusIndicator.contents = when{
			inventoryHandler.hasOutput -> OUTPUTTED
			hasInputItem -> WITH_INPUT
			else -> NONE
		}
		
		notifyUpdate(FLAG_SYNC_CLIENT or FLAG_MARK_DIRTY)
		
		if (updateInputModCounter){
			++inputModCounter
			inputModTime = world.totalWorldTime
		}
	}
	
	override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean{
		return (capability === ITEM_HANDLER_CAPABILITY && facing == DOWN) || super.hasCapability(capability, facing)
	}
	
	override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T?{
		return if (capability === ITEM_HANDLER_CAPABILITY && facing == DOWN)
			ITEM_HANDLER_CAPABILITY.cast(inventoryHandler.itemOutputCap)
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
		
		setTag("Inventory", inventoryHandler.serializeNBT())
		
		if (context == STORAGE){
			setTag("Status", statusIndicator.serializeNBT())
		}
		else if (context == NETWORK){
			linkedTable?.let {
				setInteger("StatusColor", statusIndicator.currentColor.toInt())
			}
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		linkedTable = getPosOrNull("TablePos")
		
		inventoryHandler.deserializeNBT(nbt.getCompoundTag("Inventory"))
		
		if (context == STORAGE){
			statusIndicator.deserializeNBT(nbt.getCompoundTag("Status"))
		}
		else if (context == NETWORK){
			stacksForRendering = inventoryHandler.nonEmptyStacks.toTypedArray()
			statusIndicatorColorClient = getIntegerOrNull("StatusColor")
		}
	}
}
