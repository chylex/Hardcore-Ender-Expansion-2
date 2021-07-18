package chylex.hee.game.mechanics.table.process

import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.mechanics.table.PedestalStatusIndicator
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.BLOCKED
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.PAUSED
import chylex.hee.game.mechanics.table.PedestalStatusIndicator.Process.WORKING
import chylex.hee.game.mechanics.table.interfaces.ITableContext
import chylex.hee.game.mechanics.table.interfaces.ITableInputTransformer
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Cancel
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Output
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Work
import chylex.hee.game.world.util.getTile
import chylex.hee.util.math.Pos
import chylex.hee.util.nbt.NBTItemStackList
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getListOfItemStacks
import chylex.hee.util.nbt.getPos
import chylex.hee.util.nbt.putList
import chylex.hee.util.nbt.putPos
import chylex.hee.util.nbt.use
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

abstract class ProcessManyPedestals(private val table: TileEntityBaseTable, pos: Array<BlockPos>) : ITableProcess {
	protected constructor(table: TileEntityBaseTable, nbt: TagCompound) : this(table, nbt.getLongArray(PEDESTAL_POS_TAG).map(::Pos).toTypedArray())
	
	private companion object {
		private const val PEDESTAL_POS_TAG = "PedestalPos"
		private const val LAST_INPUTS_TAG = "LastInputs"
		private const val STATE_TAG = "State"
	}
	
	final override val pedestals = pos
	
	protected abstract val whenFinished: ITableInputTransformer
	
	private var currentState: State = Work.Success
	
	private var lastInputStacks = Array(pos.size) { ItemStack.EMPTY }
	private var lastInputModCounters = Array(pos.size) { Int.MIN_VALUE }
	
	// Helpers
	
	private fun getTile(index: Int): TileEntityTablePedestal? {
		return pedestals[index].getTile(table.wrld)
	}
	
	private fun hasInputChanged(): Boolean {
		return pedestals.indices.any { index -> getTile(index)?.inputModCounter != lastInputModCounters[index] }
	}
	
	private fun setStatusIndicator(pedestals: Array<TileEntityTablePedestal>, newStatus: PedestalStatusIndicator.Process?) {
		pedestals.forEach { it.updateProcessStatus(newStatus) }
	}
	
	// Handling
	
	protected abstract fun isInputStillValid(oldInput: Array<ItemStack>, newInput: Array<ItemStack>): Boolean
	protected abstract fun onWorkTick(context: ITableContext, inputs: Array<ItemStack>): State
	
	final override fun initialize() {
		for (index in pedestals.indices) {
			val tile = getTile(index)!!
			lastInputStacks[index] = tile.itemInputCopy
			lastInputModCounters[index] = tile.inputModCounter
		}
	}
	
	final override fun revalidate(): Boolean {
		if (!hasInputChanged()) {
			return true
		}
		
		val tiles = Array(pedestals.size) { getTile(it) ?: return false }
		val newInputs = Array(tiles.size) { tiles[it].itemInputCopy }
		
		if (newInputs.any { it.isEmpty } || !isInputStillValid(lastInputStacks, newInputs)) {
			return false
		}
		
		for (index in tiles.indices) {
			lastInputStacks[index] = newInputs[index]
			lastInputModCounters[index] = tiles[index].inputModCounter
		}
		
		return true
	}
	
	final override fun tick(context: ITableContext) {
		val tiles = Array(pedestals.size) { getTile(it)!! }
		
		when (val state = currentState) {
			is Work -> {
				val currentTime = table.wrld.gameTime
				
				if (context.isPaused || tiles.any { currentTime - it.inputModTime < 20L }) {
					setStatusIndicator(tiles, PAUSED)
				}
				else if (!context.ensureDustAvailable(dustPerTick)) {
					setStatusIndicator(tiles, BLOCKED)
				}
				else {
					val inputs = Array(tiles.size) { tiles[it].itemInputCopy }
					val newState = onWorkTick(context, inputs)
					
					for ((index, tile) in tiles.withIndex()) {
						if (tile.replaceInput(inputs[index], silent = true)) {
							lastInputStacks[index] = tile.itemInputCopy
							lastInputModCounters[index] = tile.inputModCounter
						}
					}
					
					if (newState is Work) {
						setStatusIndicator(tiles, when (newState) {
							Work.Success -> WORKING.also { context.triggerWorkParticle() }
							Work.Blocked -> BLOCKED
						})
					}
					
					currentState = newState
				}
			}
			
			is Output -> {
				if (context.isPaused) {
					setStatusIndicator(tiles, PAUSED)
				}
				else if (state.pedestal.getTile<TileEntityTablePedestal>(table.wrld)?.let(context::getOutputPedestal)?.addToOutput(state.stacks) == true) {
					for (tile in tiles) {
						tile.replaceInput(tile.itemInputCopy.apply(whenFinished::transform), silent = false)
					}
					
					setStatusIndicator(tiles, null)
					context.markProcessFinished()
				}
				else {
					setStatusIndicator(tiles, BLOCKED)
				}
			}
			
			Cancel -> {
				context.markProcessFinished()
			}
		}
	}
	
	override fun dispose() {
		pedestals.indices.forEach { getTile(it)?.updateProcessStatus(null) }
	}
	
	// State
	
	protected sealed class State {
		sealed class Work : State() {
			object Success : Work()
			object Blocked : Work()
		}
		
		class Output(val stacks: Array<ItemStack>, val pedestal: BlockPos) : State() {
			constructor(stack: ItemStack, pedestal: BlockPos) : this(arrayOf(stack), pedestal)
			constructor(tag: NBTItemStackList, pedestal: BlockPos) : this(Array<ItemStack>(tag.size, tag::get), pedestal)
			
			val tag
				get() = NBTItemStackList.of(stacks.asIterable())
		}
		
		object Cancel : State()
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		putLongArray(PEDESTAL_POS_TAG, pedestals.map(BlockPos::toLong).toLongArray())
		putList(LAST_INPUTS_TAG, NBTItemStackList.of(lastInputStacks.asIterable()))
		
		val state = currentState
		
		putString(STATE_TAG, when (state) {
			is Work   -> "Work"
			is Output -> "Output".also { putList("OutputItems", state.tag); putPos("TargetPedestal", state.pedestal) }
			Cancel    -> ""
		})
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		getListOfItemStacks(LAST_INPUTS_TAG).forEachIndexed { index, stack -> lastInputStacks[index] = stack }
		
		currentState = when (getString(STATE_TAG)) {
			"Work"   -> Work.Success
			"Output" -> Output(getListOfItemStacks("OutputItems"), getPos("TargetPedestal"))
			else     -> Cancel
		}
	}
}
