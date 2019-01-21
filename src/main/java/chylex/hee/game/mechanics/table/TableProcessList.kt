package chylex.hee.game.mechanics.table
import chylex.hee.game.mechanics.table.process.ITableProcess
import chylex.hee.game.mechanics.table.process.ITableProcessSerializer
import chylex.hee.system.util.NBTObjectList
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World

class TableProcessList<T : ITableProcess> : Iterable<T>{
	val isNotEmpty
		get() = currentProcesses.isNotEmpty()
	
	private val currentProcesses = ArrayList<T>(4)
	
	fun add(process: T){
		currentProcesses.add(process)
		process.initialize()
	}
	
	fun add(processes: Collection<T>){
		processes.forEach(::add)
	}
	
	fun remove(process: T){
		currentProcesses.remove(process)
		process.dispose()
	}
	
	fun remove(predicate: (T) -> Boolean): Boolean{
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
	
	override fun iterator(): Iterator<T>{
		return currentProcesses.iterator()
	}
	
	fun serializeToList(processSerializer: ITableProcessSerializer<T>): NBTObjectList<NBTTagCompound>{
		return NBTObjectList.of(currentProcesses.map { processSerializer.writeToNBT(it) })
	}
	
	fun deserializeFromList(world: World, list: NBTObjectList<NBTTagCompound>, processSerializer: ITableProcessSerializer<T>){
		currentProcesses.clear()
		list.forEach { currentProcesses.add(processSerializer.readFromNBT(world, it)) }
	}
}
