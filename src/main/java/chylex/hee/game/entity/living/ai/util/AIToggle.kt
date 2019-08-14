package chylex.hee.game.entity.living.ai.util
import chylex.hee.system.util.delegate.NotifyOnChange
import net.minecraft.entity.ai.EntityAIBase
import net.minecraft.entity.ai.EntityAITasks

class AIToggle{
	private class Entry(private val taskList: EntityAITasks, private val priority: Int, private val instance: EntityAIBase){
		fun update(newValue: Boolean){
			if (newValue){
				taskList.addTask(priority, instance)
			}
			else{
				taskList.removeTask(instance)
			}
		}
		
		fun conflicts(other: Entry): Boolean{
			return taskList === other.taskList && (priority == other.priority || instance === other.instance)
		}
		
		override fun toString(): String{
			return "$priority, ${instance::class.java.simpleName}"
		}
	}
	
	companion object{
		fun EntityAITasks.addTask(priority: Int, task: EntityAIBase, controller: AIToggle){
			controller.addEntry(Entry(this, priority, task))
		}
	}
	
	// Instance
	
	var enabled by NotifyOnChange(false, ::onChange)
	
	private val entries = ArrayList<Entry>(1)
	
	private fun addEntry(newEntry: Entry){
		val conflict = entries.find(newEntry::conflicts)
		
		if (conflict != null){
			throw UnsupportedOperationException("task ($newEntry) is conflicting with ($conflict) in AI toggle group")
		}
		
		entries.add(newEntry)
		
		if (enabled){
			newEntry.update(true)
		}
	}
	
	private fun onChange(newValue: Boolean){
		for(entry in entries){
			entry.update(newValue)
		}
	}
}
