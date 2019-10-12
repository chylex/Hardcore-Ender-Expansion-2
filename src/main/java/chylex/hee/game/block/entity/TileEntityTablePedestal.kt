package chylex.hee.game.block.entity
import chylex.hee.HEE
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.BlockTablePedestal.Companion.IS_LINKED
import chylex.hee.game.block.entity.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.mechanics.table.PedestalInventoryHandler
import chylex.hee.game.mechanics.table.PedestalStatusIndicator
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Contents.NONE
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Contents.OUTPUTTED
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Contents.WITH_INPUT
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.DEDICATED_OUTPUT
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.getTile
import chylex.hee.system.util.isLoaded
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.setPos
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.updateState
import chylex.hee.system.util.with
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
import java.util.Random

class TileEntityTablePedestal : TileEntityBase(){
	companion object{
		private const val TABLE_POS_TAG = "TablePos"
		private const val INVENTORY_TAG = "Inventory"
		private const val STATUS_TAG = "Status"
		private const val STATUS_COLOR_TAG = "StatusColor"
		
		val FX_ITEM_UPDATE = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				val player = HEE.proxy.getClientSidePlayer() ?: return
				
				PARTICLE_ITEM_UPDATE.spawn(Point(pos.up(), 12), rand)
				Sounds.ENTITY_ITEM_PICKUP.playClient(player.posVec, SoundCategory.PLAYERS, volume = 0.22F, pitch = rand.nextFloat(0.6F, 3.4F))
			}
		}
		
		private val PARTICLE_ITEM_UPDATE = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(scale = 1.33F),
			pos = Constant(0.15F, DOWN) + InBox(0.45F)
		)
	}
	
	// Properties (Tables)
	
	private var linkedTable by NotifyOnChange<BlockPos?>(null, ::onLinkedStatusChanged)
	
	private val linkedTableTile: TileEntityBaseTable?
		get() = linkedTable?.getTile(world)
	
	val hasLinkedTable
		get() = linkedTable != null
	
	val tableIndicatorColor: IntColor?
		get() = linkedTable?.takeIf { it.isLoaded(world) }?.getTile<TileEntityBaseTable>(world)?.tableIndicatorColor
	
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
	
	val outputComparatorStrength
		get() = inventoryHandler.outputComparatorStrength
	
	var stacksForRendering = emptyArray<ItemStack>()
		private set
	
	private var lastSmokeTime = Long.MIN_VALUE
	
	// Behavior (General)
	
	fun <T> SyncOnChange(initialValue: T) = NotifyOnChange(initialValue){
		-> if (isLoaded) notifyUpdate(FLAG_SYNC_CLIENT or FLAG_MARK_DIRTY)
	}
	
	fun onPedestalDestroyed(dropTableLink: Boolean){
		linkedTableTile?.tryUnlinkPedestal(this, dropTableLink)
		linkedTable = null // must reset state because the method is called twice if the player is in creative mode
		
		dropAllItems()
	}
	
	private fun spawnSmokeParticles(){
		val currentTime = world.totalTime
		
		if (lastSmokeTime != currentTime){
			lastSmokeTime = currentTime
			PacketClientFX(FX_ITEM_UPDATE, FxBlockData(pos)).sendToAllAround(this, 16.0)
		}
	}
	
	// Behavior (Tables)
	
	fun setLinkedTable(tile: TileEntityBaseTable){
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
	
	fun onTableUnlinked(tile: TileEntityBaseTable, dropTableLink: Boolean){
		unlinkTableInternal(tile, if (dropTableLink) pos.up() else null)
	}
	
	fun onTableDestroyed(tile: TileEntityBaseTable, dropTableLink: Boolean){
		unlinkTableInternal(tile, if (dropTableLink) tile.pos else null)
	}
	
	private fun unlinkTableInternal(tile: TileEntityBaseTable, dropTableLinkAt: BlockPos?){
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
			pos.updateState(world, ModBlocks.TABLE_PEDESTAL, FLAG_SYNC_CLIENT){ it.with(IS_LINKED, linkedTable != null) }
			statusIndicator.process = null
		}
	}
	
	fun updateProcessStatus(newStatus: PedestalStatusIndicator.Process?){
		statusIndicator.process = newStatus
	}
	
	// Behavior (Inventory)
	
	fun addToInput(stack: ItemStack): Boolean{
		if (!isDedicatedOutput && inventoryHandler.addToInput(stack)){
			spawnSmokeParticles()
			return true
		}
		
		return false
	}
	
	fun addToOutput(stacks: Array<ItemStack>): Boolean{
		if (inventoryHandler.addToOutput(stacks)){
			spawnSmokeParticles()
			return true
		}
		
		return false
	}
	
	fun replaceInput(newInput: ItemStack, silent: Boolean): Boolean{
		if (inventoryHandler.replaceInput(newInput, silent)){
			if (!silent){
				spawnSmokeParticles()
			}
			
			return true
		}
		
		return false
	}
	
	fun moveOutputToPlayerInventory(inventory: InventoryPlayer){
		if (inventoryHandler.moveOutputToPlayerInventory(inventory)){
			spawnSmokeParticles()
		}
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
			inputModTime = world.totalTime
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
	
	// Client side
	
	@Sided(Side.CLIENT)
	override fun getRenderBoundingBox(): AxisAlignedBB{
		return AxisAlignedBB(pos, pos.add(1, 2, 1))
	}
	
	// Serialization
	
	override fun shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState): Boolean{
		return newState.block != oldState.block
	}
	
	override fun writeNBT(nbt: TagCompound, context: Context) = with(nbt){
		linkedTable?.let {
			setPos(TABLE_POS_TAG, it)
		}
		
		setTag(INVENTORY_TAG, inventoryHandler.serializeNBT())
		
		if (context == STORAGE){
			setTag(STATUS_TAG, statusIndicator.serializeNBT())
		}
		else if (context == NETWORK){
			linkedTable?.let {
				setInteger(STATUS_COLOR_TAG, statusIndicator.currentColor.i)
			}
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = with(nbt){
		linkedTable = getPosOrNull(TABLE_POS_TAG)
		
		inventoryHandler.deserializeNBT(nbt.getCompoundTag(INVENTORY_TAG))
		
		if (context == STORAGE){
			statusIndicator.deserializeNBT(nbt.getCompoundTag(STATUS_TAG))
		}
		else if (context == NETWORK){
			stacksForRendering = inventoryHandler.nonEmptyStacks.toTypedArray()
			statusIndicatorColorClient = getIntegerOrNull(STATUS_COLOR_TAG)
		}
	}
}
