package chylex.hee.game.block.entity.base
import chylex.hee.game.block.BlockAbstractTable
import chylex.hee.game.block.BlockAbstractTable.Companion.TIER
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.game.mechanics.dust.DustType
import chylex.hee.game.mechanics.table.TableEnergyClusterHandler
import chylex.hee.game.mechanics.table.TableLinkedPedestalHandler
import chylex.hee.game.mechanics.table.TableParticleHandler
import chylex.hee.game.mechanics.table.TableProcessList
import chylex.hee.game.mechanics.table.interfaces.ITableContext
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcessSerializer
import chylex.hee.game.mechanics.table.process.ProcessSupportingItemHolder
import chylex.hee.system.util.NBTList.Companion.putList
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.use
import net.minecraft.item.Item
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntityType
import org.apache.commons.lang3.math.Fraction

abstract class TileEntityBaseTable(type: TileEntityType<out TileEntityBaseTable>) : TileEntityBase(type), ITickableTileEntity{
	private companion object{
		private const val MAX_CLUSTER_DISTANCE = 12
		private const val MAX_PEDESTAL_DISTANCE = 6
		
		private const val PROCESS_REFRESH_RATE = 10
		
		private const val PEDESTAL_INFO_TAG = "PedestalInfo"
		private const val CLUSTER_INFO_TAG = "ClusterInfo"
		private const val PROCESSES_TAG = "Processes"
		private const val DUST_FRACTION_N_TAG = "DustAmountN"
		private const val DUST_FRACTION_D_TAG = "DustAmountD"
	}
	
	var maxInputPedestals = 0
		private set
	
	abstract val tableIndicatorColor: IntColor
	open val tableDustType: DustType? = null
	
	protected abstract val processSerializer: ITableProcessSerializer
	protected abstract val processTickRate: Int
	
	private var tickCounterRefresh = 0
	private var tickCounterProcessing = 0
	
	private val currentProcesses = TableProcessList()
	private var storedDust = Fraction.ZERO
	
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
		val state = pos.getState(wrld)
		val block = state.block as BlockAbstractTable
		
		maxInputPedestals = when(state[TIER] - block.minAllowedTier){
			2 -> 7
			1 -> 5
			0 -> 3
			else -> 0
		}
	}
	
	final override fun tick(){
		if (wrld.isRemote || !wrld.isAreaLoaded(pos, MAX_CLUSTER_DISTANCE)){
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
				val isPaused = wrld.getRedstonePowerFromNeighbors(pos) > 0
				
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
		
		override fun ensureDustAvailable(amount: Fraction): Boolean{
			if (amount == ITableProcess.NO_DUST || storedDust >= amount){
				return true
			}
			
			val dustType = tableDustType
			val jar = pos.up().getTile<TileEntityJarODust>(wrld)
			
			if (dustType == null || jar == null){
				return false
			}
			
			while(storedDust < amount){
				if (jar.layers.removeDust(DustLayers.Side.BOTTOM, dustType, 1).isEmpty){
					return false
				}
				
				storedDust = storedDust.add(Fraction.ONE)
			}
			
			return true
		}
		
		override fun requestUseResources(): Boolean{
			if (clusterHandler.drainEnergy(process.energyPerTick)){
				storedDust = storedDust.subtract(process.dustPerTick)
				return true
			}
			
			return false
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
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		if (context == STORAGE){
			put(PEDESTAL_INFO_TAG, pedestalHandler.serializeNBT())
			put(CLUSTER_INFO_TAG, clusterHandler.serializeNBT())
			putList(PROCESSES_TAG, currentProcesses.serializeToList(processSerializer))
			
			putInt(DUST_FRACTION_N_TAG, storedDust.numerator)
			putInt(DUST_FRACTION_D_TAG, storedDust.denominator)
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		if (context == STORAGE){
			pedestalHandler.deserializeNBT(getCompound(PEDESTAL_INFO_TAG))
			clusterHandler.deserializeNBT(getCompound(CLUSTER_INFO_TAG))
			currentProcesses.deserializeFromList(wrld, getListOfCompounds(PROCESSES_TAG), processSerializer)
			
			storedDust = Fraction.getFraction(getInt(DUST_FRACTION_N_TAG), getInt(DUST_FRACTION_D_TAG))
		}
	}
}
