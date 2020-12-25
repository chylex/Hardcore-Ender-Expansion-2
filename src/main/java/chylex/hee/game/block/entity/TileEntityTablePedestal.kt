package chylex.hee.game.block.entity

import chylex.hee.HEE
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.block.BlockTablePedestal.Companion.IS_LINKED
import chylex.hee.game.block.entity.base.TileEntityBase
import chylex.hee.game.block.entity.base.TileEntityBase.Context.NETWORK
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.entity.posVec
import chylex.hee.game.inventory.isNotEmpty
import chylex.hee.game.mechanics.table.PedestalInventoryHandler
import chylex.hee.game.mechanics.table.PedestalStatusIndicator
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Contents.NONE
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Contents.OUTPUTTED
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Contents.WITH_INPUT
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.BLOCKED
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.DEDICATED_OUTPUT
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.PAUSED
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.SUPPORTING_ITEM
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.WORKING
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.FLAG_RENDER_IMMEDIATE
import chylex.hee.game.world.FLAG_SYNC_CLIENT
import chylex.hee.game.world.getState
import chylex.hee.game.world.getTile
import chylex.hee.game.world.isLoaded
import chylex.hee.game.world.playClient
import chylex.hee.game.world.setState
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.init.ModTileEntities
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxBlockData
import chylex.hee.network.fx.FxBlockHandler
import chylex.hee.system.color.IntColor
import chylex.hee.system.delegate.NotifyOnChange
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Sounds
import chylex.hee.system.random.nextFloat
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getIntegerOrNull
import chylex.hee.system.serialization.getPosOrNull
import chylex.hee.system.serialization.putPos
import chylex.hee.system.serialization.use
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Direction
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
import java.util.Random

class TileEntityTablePedestal(type: TileEntityType<TileEntityTablePedestal>) : TileEntityBase(type) {
	constructor() : this(ModTileEntities.TABLE_PEDESTAL)
	
	companion object {
		private const val TABLE_POS_TAG = "TablePos"
		private const val INVENTORY_TAG = "Inventory"
		private const val STATUS_TAG = "Status"
		private const val STATUS_COLOR_TAG = "StatusColor"
		
		val FX_ITEM_UPDATE = object : FxBlockHandler() {
			override fun handle(pos: BlockPos, world: World, rand: Random) {
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
		get() = linkedTable?.getTile(wrld)
	
	val hasLinkedTable
		get() = linkedTable != null
	
	val tableIndicatorColor: IntColor?
		get() = linkedTable?.takeIf { it.isLoaded(wrld) }?.getTile<TileEntityBaseTable>(wrld)?.tableIndicatorColor
	
	private val statusIndicator = PedestalStatusIndicator(this)
	
	var statusIndicatorColorClient by NotifyOnChange<Int?>(null) {
		-> wrld.notifyBlockUpdate(pos, blockState, blockState, FLAG_RENDER_IMMEDIATE)
	}
	
	var isDedicatedOutput
		get() = statusIndicator.process == DEDICATED_OUTPUT
		set(value) {
			if (value) {
				statusIndicator.process = DEDICATED_OUTPUT
				inventoryHandler.dropInputItem(wrld, pos.up())
			}
			else {
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
		get() = when(statusIndicator.process ?: statusIndicator.contents) {
			PAUSED                      -> 2
			WORKING                     -> 3
			BLOCKED                     -> 4
			WITH_INPUT, SUPPORTING_ITEM -> 1
			OUTPUTTED, DEDICATED_OUTPUT -> 6 + inventoryHandler.nonEmptyOutputSlots
			else                        -> 0
		}
	
	var stacksForRendering = emptyArray<ItemStack>()
		private set
	
	private var lastSmokeTime = Long.MIN_VALUE
	
	// Behavior (General)
	
	fun <T> SyncOnChange(initialValue: T) = NotifyOnChange(initialValue) {
		-> if (isLoaded) notifyUpdate(FLAG_SYNC_CLIENT or FLAG_MARK_DIRTY)
	}
	
	fun onPedestalDestroyed(dropTableLink: Boolean) {
		linkedTableTile?.tryUnlinkPedestal(this, dropTableLink)
		linkedTable = null // must reset state because the method is called twice if the player is in creative mode
		
		dropAllItems()
	}
	
	private fun spawnSmokeParticles() {
		val currentTime = wrld.totalTime
		
		if (lastSmokeTime != currentTime) {
			lastSmokeTime = currentTime
			PacketClientFX(FX_ITEM_UPDATE, FxBlockData(pos)).sendToAllAround(this, 16.0)
		}
	}
	
	// Behavior (Tables)
	
	fun setLinkedTable(tile: TileEntityBaseTable) {
		val currentlyLinkedTable = linkedTableTile
		
		if (tile !== currentlyLinkedTable) {
			currentlyLinkedTable?.tryUnlinkPedestal(this, dropTableLink = false)
			linkedTable = tile.pos
		}
	}
	
	fun resetLinkedTable(dropTableLink: Boolean) {
		linkedTableTile?.tryUnlinkPedestal(this, dropTableLink)
	}
	
	fun requestMarkAsOutput(): Boolean {
		return linkedTableTile?.tryMarkInputPedestalAsOutput(this) == true
	}
	
	fun onTableUnlinked(tile: TileEntityBaseTable, dropTableLink: Boolean) {
		unlinkTableInternal(tile, if (dropTableLink) pos.up() else null)
	}
	
	fun onTableDestroyed(tile: TileEntityBaseTable, dropTableLink: Boolean) {
		unlinkTableInternal(tile, if (dropTableLink) tile.pos else null)
	}
	
	private fun unlinkTableInternal(tile: TileEntityBaseTable, dropTableLinkAt: BlockPos?) {
		linkedTable?.takeIf { it == tile.pos }?.let {
			dropTableLinkAt?.let(::spawnTableLinkAt)
			linkedTable = null
		}
	}
	
	private fun spawnTableLinkAt(pos: BlockPos) {
		val rand = wrld.rand
		
		EntityItem(wrld, pos.x + rand.nextFloat(0.25, 0.75), pos.y + rand.nextFloat(0.25, 0.75), pos.z + rand.nextFloat(0.25, 0.75), ItemStack(ModItems.TABLE_LINK)).apply {
			setDefaultPickupDelay()
			throwerId = BlockTablePedestal.DROPPED_ITEM_THROWER
			wrld.addEntity(this)
		}
	}
	
	private fun onLinkedStatusChanged() {
		if (world != null) {
			val state = pos.getState(wrld)
			
			if (state.block === ModBlocks.TABLE_PEDESTAL) {
				pos.setState(wrld, state.with(IS_LINKED, linkedTable != null))
			}
			
			statusIndicator.process = null
		}
	}
	
	fun updateProcessStatus(newStatus: PedestalStatusIndicator.Process?) {
		statusIndicator.process = newStatus
	}
	
	// Behavior (Inventory)
	
	fun addToInput(stack: ItemStack): Boolean {
		if (!isDedicatedOutput && inventoryHandler.addToInput(stack)) {
			spawnSmokeParticles()
			return true
		}
		
		return false
	}
	
	fun addToOutput(stacks: Array<ItemStack>): Boolean {
		if (inventoryHandler.addToOutput(stacks)) {
			spawnSmokeParticles()
			return true
		}
		
		return false
	}
	
	fun replaceInput(newInput: ItemStack, silent: Boolean): Boolean {
		if (inventoryHandler.replaceInput(newInput, silent)) {
			if (!silent) {
				spawnSmokeParticles()
			}
			
			return true
		}
		
		return false
	}
	
	fun moveOutputToPlayerInventory(inventory: PlayerInventory) {
		if (inventoryHandler.moveOutputToPlayerInventory(inventory)) {
			spawnSmokeParticles()
		}
	}
	
	fun dropAllItems() {
		inventoryHandler.dropAllItems(wrld, pos.up())
	}
	
	private fun onInventoryUpdated(updateInputModCounter: Boolean) {
		statusIndicator.contents = when {
			inventoryHandler.hasOutput -> OUTPUTTED
			hasInputItem               -> WITH_INPUT
			else                       -> NONE
		}
		
		notifyUpdate(FLAG_SYNC_CLIENT or FLAG_MARK_DIRTY)
		
		if (updateInputModCounter) {
			++inputModCounter
			inputModTime = wrld.totalTime
		}
	}
	
	override fun <T : Any?> getCapability(capability: Capability<T>, facing: Direction?): LazyOptional<T> {
		return if (capability === ITEM_HANDLER_CAPABILITY && facing == DOWN)
			inventoryHandler.itemOutputCap.cast()
		else
			super.getCapability(capability, facing)
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	override fun getRenderBoundingBox(): AxisAlignedBB {
		return AxisAlignedBB(pos, pos.add(1, 2, 1))
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		linkedTable?.let {
			putPos(TABLE_POS_TAG, it)
		}
		
		put(INVENTORY_TAG, inventoryHandler.serializeNBT())
		
		if (context == STORAGE) {
			put(STATUS_TAG, statusIndicator.serializeNBT())
		}
		else if (context == NETWORK) {
			linkedTable?.let {
				putInt(STATUS_COLOR_TAG, statusIndicator.currentColor.i)
			}
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		linkedTable = getPosOrNull(TABLE_POS_TAG)
		
		inventoryHandler.deserializeNBT(nbt.getCompound(INVENTORY_TAG))
		
		if (context == STORAGE) {
			statusIndicator.deserializeNBT(nbt.getCompound(STATUS_TAG))
		}
		else if (context == NETWORK) {
			stacksForRendering = inventoryHandler.nonEmptyStacks.toTypedArray()
			statusIndicatorColorClient = getIntegerOrNull(STATUS_COLOR_TAG)
		}
	}
}
