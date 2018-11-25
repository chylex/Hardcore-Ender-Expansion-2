package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockAbstractTable
import chylex.hee.game.block.BlockAbstractTable.Companion.TIER
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.game.mechanics.table.TableEnergyClusterHandler
import chylex.hee.game.mechanics.table.TableLinkedPedestalHandler
import chylex.hee.game.mechanics.table.process.ITableContext
import chylex.hee.game.mechanics.table.process.ITableProcess
import chylex.hee.game.mechanics.table.process.ITableProcessSerializer
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.NBTObjectList
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.getState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable
import net.minecraft.world.World
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

abstract class TileEntityBaseTable<T : ITableProcess> : TileEntityBase(), ITickable{
	private companion object{
		private const val MAX_CLUSTER_DISTANCE = 12
		private const val MAX_PEDESTAL_DISTANCE = 6
		
		private const val PROCESS_REFRESH_RATE = 10
	}
	
	var maxInputPedestals = 0
		private set
	
	abstract val tableIndicatorColor: Int
	
	protected abstract val processSerializer: ITableProcessSerializer<T>
	protected abstract val processTickRate: Int
	
	private var tickCounterRefresh = 0
	private var tickCounterProcessing = 0
	
	private val currentProcesses = ArrayList<T>(4)
	
	@Suppress("LeakingThis")
	private val pedestalHandler = TableLinkedPedestalHandler(this, MAX_PEDESTAL_DISTANCE)
	
	@Suppress("LeakingThis")
	private val clusterHandler = TableEnergyClusterHandler(this, MAX_CLUSTER_DISTANCE)
	
	// Utilities
	
	inner class MarkDirtyOnChange<T>(initialValue: T) : ObservableProperty<T>(initialValue){
		override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T){
			if (isLoaded && oldValue != newValue){
				markDirty()
			}
		}
	}
	
	private val predicatePedestalBusy: (TileEntityTablePedestal) -> Boolean =
		{ val pos = it.pos; currentProcesses.any { process -> process.pedestals.contains(pos) } }
	
	private val predicatePedestalHasInput: (TileEntityTablePedestal) -> Boolean =
		{ it.hasInputItem }
	
	// Behavior
	
	override fun firstTick(){
		val state = pos.getState(world)
		val block = state.block as? BlockAbstractTable ?: return // TODO
		
		maxInputPedestals = when(state.getValue(TIER) - block.minAllowedTier){
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
			
			val unassignedPedestals = pedestalHandler.pedestalTiles.filter(predicatePedestalHasInput).filterNot(predicatePedestalBusy).toList()
			
			if (unassignedPedestals.isNotEmpty()){
				tickCounterProcessing = 0
				
				for(newProcess in createNewProcesses(unassignedPedestals)){
					currentProcesses.add(newProcess)
					newProcess.initialize()
				}
			}
		}
		
		currentProcesses.removeIf { !it.revalidate() }
		
		if (++tickCounterProcessing >= processTickRate){
			tickCounterProcessing = 0
			
			val iterator = currentProcesses.iterator()
			
			while(iterator.hasNext()){
				val process = iterator.next()
				process.tick(createProcessingContext(process, iterator))
			}
		}
	}
	
	fun onTableDestroyed(dropTableLink: Boolean){
		pedestalHandler.pedestalTiles.forEach { it.onTableDestroyed(this, dropTableLink) }
		pedestalHandler.onAllPedestalsUnlinked() // must reset state because the method is called twice if the player is in creative mode
	}
	
	fun tryLinkPedestal(pedestal: TileEntityTablePedestal): Boolean{
		return pedestalHandler.tryLinkPedestal(pedestal)
	}
	
	fun tryUnlinkPedestal(pedestal: TileEntityTablePedestal, dropTableLink: Boolean): Boolean{
		return pedestalHandler.tryUnlinkPedestal(pedestal, dropTableLink)
	}
	
	// Processing
	
	protected abstract fun createNewProcesses(unassignedPedestals: List<TileEntityTablePedestal>): List<T>
	
	private fun createProcessingContext(process: ITableProcess, iterator: MutableIterator<ITableProcess>) = object : ITableContext{
		override fun requestUseResources(): Boolean{
			return clusterHandler.drainEnergy(process.energyPerTick)
		}
		
		override fun markProcessFinished(){
			iterator.remove()
		}
	}
	
	// Serialization
	
	override fun setWorldCreate(world: World){
		this.world = world // ensures world is available in readNBT
	}
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		if (context == STORAGE){
			setTag("PedestalInfo", pedestalHandler.serializeNBT())
			setTag("ClusterInfo", clusterHandler.serializeNBT())
			setList("Processes", NBTObjectList.of(currentProcesses.map { processSerializer.writeToNBT(it) }))
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		if (context == STORAGE){
			pedestalHandler.deserializeNBT(getCompoundTag("PedestalInfo"))
			clusterHandler.deserializeNBT(getCompoundTag("ClusterInfo"))
			
			currentProcesses.clear()
			getListOfCompounds("Processes").forEach { currentProcesses.add(processSerializer.readFromNBT(world, it)) }
		}
	}
}
