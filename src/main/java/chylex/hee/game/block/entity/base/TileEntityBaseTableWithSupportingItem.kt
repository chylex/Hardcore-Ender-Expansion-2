package chylex.hee.game.block.entity.base
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.process.ProcessSupportingItemBlocker
import chylex.hee.game.mechanics.table.process.ProcessSupportingItemHolder
import chylex.hee.game.mechanics.table.process.serializer.MultiProcessSerializer.Companion.Mapping
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.BlockPos

abstract class TileEntityBaseTableWithSupportingItem(type: TileEntityType<out TileEntityBaseTableWithSupportingItem>) : TileEntityBaseTable(type){
	companion object{
		val SUPPORTING_ITEM_MAPPINGS = arrayOf(
			Mapping("Supporting", ::ProcessSupportingItemHolder),
			Mapping("Blocking", ::ProcessSupportingItemBlocker)
		)
	}
	
	protected abstract fun isSupportingItem(stack: ItemStack): Boolean
	protected abstract fun getProcessFor(pedestalPos: BlockPos, stack: ItemStack): ITableProcess?
	
	final override fun createNewProcesses(unassignedPedestals: List<TileEntityTablePedestal>): List<ITableProcess>{
		val newProcesses = ArrayList<ITableProcess>(1)
		var hasSupportingItem = hasSupportingItem
		
		for(pedestal in unassignedPedestals){
			if (isSupportingItem(pedestal.itemInputCopy)){
				newProcesses.add(ProcessSupportingItemHolder(this, pedestal.pos))
				hasSupportingItem = true
			}
			else{
				val process = getProcessFor(pedestal.pos, pedestal.itemInputCopy)
				
				if (process != null){
					if (totalFreePedestals - newProcesses.size <= 1 && !hasSupportingItem){
						newProcesses.add(ProcessSupportingItemBlocker(this, pedestal.pos))
					}
					else{
						newProcesses.add(process)
					}
				}
			}
		}
		
		return newProcesses
	}
}
