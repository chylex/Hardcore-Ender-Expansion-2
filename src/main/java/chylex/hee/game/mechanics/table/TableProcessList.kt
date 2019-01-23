package chylex.hee.game.mechanics.table
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcessSerializer
import chylex.hee.system.util.NBTObjectList
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World

class TableProcessList : Iterable<ITableProcess>{
	val isNotEmpty
		get() = currentProcesses.isNotEmpty()
	
	private val currentProcesses = ArrayList<ITableProcess>(4)
	
	fun add(process: ITableProcess){
		currentProcesses.add(process)
		process.initialize()
	}
	
	fun add(processes: Collection<ITableProcess>){
		processes.forEach(::add)
	}
	
	fun remove(process: ITableProcess){
		currentProcesses.remove(process)
		process.dispose()
	}
	
	fun remove(predicate: (ITableProcess) -> Boolean): Boolean{
		var removedAny = false
		
		for(index in currentProcesses.indices.reversed()){
			val process = currentProcesses[index]
			
			if (predicate(process)){
				remove(process)
				removedAny = true
			}
		}
		
		return removedAny
	}
	
	override fun iterator(): Iterator<ITableProcess>{
		return currentProcesses.iterator()
	}
	
	fun serializeToList(processSerializer: ITableProcessSerializer): NBTObjectList<NBTTagCompound>{
		return NBTObjectList.of(currentProcesses.map { processSerializer.writeToNBT(it) })
	}
	
	fun deserializeFromList(world: World, list: NBTObjectList<NBTTagCompound>, processSerializer: ITableProcessSerializer){
		currentProcesses.clear()
		list.forEach { currentProcesses.add(processSerializer.readFromNBT(world, it)) }
	}
}
