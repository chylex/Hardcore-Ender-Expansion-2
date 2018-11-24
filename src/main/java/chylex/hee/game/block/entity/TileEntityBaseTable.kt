package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockAbstractTable
import chylex.hee.game.block.BlockAbstractTable.Companion.TIER
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.game.mechanics.table.TableLinkedPedestalHandler
import chylex.hee.game.mechanics.table.process.ITableProcess
import chylex.hee.system.util.getState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable

abstract class TileEntityBaseTable<T : ITableProcess> : TileEntityBase(), ITickable{
	private companion object{
		private const val MAX_PEDESTAL_DISTANCE = 6
	}
	
	var maxInputPedestals = 0
		private set
	
	abstract val tableIndicatorColor: Int
	
	@Suppress("LeakingThis")
	private val pedestalHandler = TableLinkedPedestalHandler(this, MAX_PEDESTAL_DISTANCE)
	
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
	
	// Serialization
	
	override fun writeNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		if (context == STORAGE){
			setTag("PedestalInfo", pedestalHandler.serializeNBT())
		}
	}
	
	override fun readNBT(nbt: NBTTagCompound, context: Context) = with(nbt){
		if (context == STORAGE){
			pedestalHandler.deserializeNBT(getCompoundTag("PedestalInfo"))
		}
	}
}
