package chylex.hee.game.block.entity.base
import chylex.hee.game.block.BlockAbstractTable
import chylex.hee.game.block.BlockAbstractTable.Companion.TIER
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.mechanics.table.TableEnergyClusterHandler
import chylex.hee.game.mechanics.table.TableLinkedPedestalHandler
import chylex.hee.game.mechanics.table.TableParticleHandler
import chylex.hee.game.mechanics.table.TableProcessList
import chylex.hee.game.mechanics.table.interfaces.ITableContext
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcessSerializer
import chylex.hee.game.mechanics.table.process.ProcessSupportingItemHolder
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.get
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.getState
import net.minecraft.item.Item
import net.minecraft.util.ITickable
import net.minecraft.world.World

abstract class TileEntityBaseTable : TileEntityBase(), ITickable{
	private companion object{
		private const val MAX_CLUSTER_DISTANCE = 12
		private const val MAX_PEDESTAL_DISTANCE = 6
		
		private const val PROCESS_REFRESH_RATE = 10
		
		private const val PEDESTAL_INFO_TAG = "PedestalInfo"
		private const val CLUSTER_INFO_TAG = "ClusterInfo"
		private const val PROCESSES_TAG = "Processes"
	}
	
	var maxInputPedestals = 0
		private set
	
	abstract val tableIndicatorColor: IntColor
	
	protected abstract val processSerializer: ITableProcessSerializer
	protected abstract val processTickRate: Int
	
	private var tickCounterRefresh = 0
	private var tickCounterProcessing = 0
	
	private val currentProcesses = TableProcessList()
	
	@Suppress("LeakingThis")
	private val pedestalHandler = TableLinkedPedestalHandler(this, MAX_PEDESTAL_DISTANCE)
	
	@Suppress("LeakingThis")
	private val clusterHandler = TableEnergyClusterHandler(this, MAX_CLUSTER_DISTANCE)
	
	@Suppress("LeakingThis")
	val particleHandler = TableParticleHandler(this)
	
	// Utilities
	
	fun <T> MarkDirtyOnChange(initialValue: T) = NotifyOnChange(initialValue){
		-> if (isLoaded) markDirty()
	}
	
	private fun isPedestalBusy(pedestal: TileEntityTablePedestal): Boolean{
		val pos = pedestal.pos
		return currentProcesses.any { process -> process.pedestals.contains(pos) }
	}
	
	// Behavior
	
	override fun firstTick(){
		val state = pos.getState(world)
		val block = state.block as BlockAbstractTable
		
		maxInputPedestals = when(state[TIER] - block.minAllowedTier){
			2 -> 7
			1 -> 5
			0 -> 3
			else -> 0
		}
	}
	
	final override fun update(){
		if (world.isRemote || !world.isAreaLoaded(pos, MAX_CLUSTER_DISTANCE)){
			return
		}
		
		if (++tickCounterRefresh >= PROCESS_REFRESH_RATE){
			tickCounterRefresh = 0
			
			val unassignedPedestals = pedestalHandler.inputPedestalTiles.filter { it.hasInputItem && !isPedestalBusy(it) }
			
			if (unassignedPedestals.isNotEmpty()){
				val newProcesses = createNewProcesses(unassignedPedestals)
				
				if (newProcesses.isNotEmpty()){
					tickCounterProcessing = 0
					currentProcesses.add(newProcesses)
					markDirty()
				}
			}
		}
		
		if (currentProcesses.revalidate()){
			markDirty()
		}
		
		if (++tickCounterProcessing >= processTickRate){
			tickCounterProcessing = 0
			
			if (currentProcesses.isNotEmpty){
				val isPaused = world.getRedstonePowerFromNeighbors(pos) > 0
				
				currentProcesses.remove {
					createProcessingContext(it, isPaused).apply(it::tick).isFinished
				}
				
				markDirty()
			}
		}
		
		particleHandler.tick(processTickRate)
	}
	
	fun onTableDestroyed(dropTableLink: Boolean){
		pedestalHandler.inputPedestalTiles.forEach { it.onTableDestroyed(this, dropTableLink) }
		pedestalHandler.dedicatedOutputPedestalTile?.onTableDestroyed(this, dropTableLink)
		pedestalHandler.onAllPedestalsUnlinked() // must reset state because the method is called twice if the player is in creative mode
	}
	
	fun tryLinkPedestal(pedestal: TileEntityTablePedestal): Boolean{
		return pedestalHandler.tryLinkPedestal(pedestal)
	}
	
	fun tryUnlinkPedestal(pedestal: TileEntityTablePedestal, dropTableLink: Boolean): Boolean{
		currentProcesses.remove(pedestal)
		return pedestalHandler.tryUnlinkPedestal(pedestal, dropTableLink)
	}
	
	fun tryMarkInputPedestalAsOutput(pedestal: TileEntityTablePedestal): Boolean{
		currentProcesses.remove(pedestal)
		return pedestalHandler.tryMarkInputPedestalAsOutput(pedestal)
	}
	
	// Processing
	
	protected abstract fun createNewProcesses(unassignedPedestals: List<TileEntityTablePedestal>): List<ITableProcess>
	
	private fun createProcessingContext(process: ITableProcess, isPaused: Boolean) = object : ITableContext{
		var isFinished = false
		
		override val isPaused = isPaused
		
		override fun requestUseResources(): Boolean{
			return clusterHandler.drainEnergy(process.energyPerTick)
		}
		
		override fun requestUseSupportingItem(item: Item, amount: Int): Boolean{
			return currentProcesses.firstOrNull { it is ProcessSupportingItemHolder && it.useItem(item, amount) } != null
		}
		
		override fun getOutputPedestal(candidate: TileEntityTablePedestal): TileEntityTablePedestal {
			return pedestalHandler.dedicatedOutputPedestalTile ?: candidate
		}
		
		override fun triggerWorkParticle(){
			particleHandler.onPedestalsTicked(process.pedestals)
		}
		
		override fun markProcessFinished(){
			isFinished = true
		}
	}
	
	// Serialization
	
	override fun setWorldCreate(world: World){
		this.world = world // ensures world is available in readNBT
	}
	
	override fun writeNBT(nbt: TagCompound, context: Context) = with(nbt){
		if (context == STORAGE){
			setTag(PEDESTAL_INFO_TAG, pedestalHandler.serializeNBT())
			setTag(CLUSTER_INFO_TAG, clusterHandler.serializeNBT())
			setList(PROCESSES_TAG, currentProcesses.serializeToList(processSerializer))
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = with(nbt){
		if (context == STORAGE){
			pedestalHandler.deserializeNBT(getCompoundTag(PEDESTAL_INFO_TAG))
			clusterHandler.deserializeNBT(getCompoundTag(CLUSTER_INFO_TAG))
			currentProcesses.deserializeFromList(world, getListOfCompounds(PROCESSES_TAG), processSerializer)
		}
	}
}
