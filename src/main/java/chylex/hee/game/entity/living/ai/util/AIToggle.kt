package chylex.hee.game.entity.living.ai.util
import chylex.hee.system.util.delegate.NotifyOnChange
import net.minecraft.entity.ai.EntityAIBase
import net.minecraft.entity.ai.EntityAITasks

class AIToggle{
	companion object{
		fun EntityAITasks.addTask(priority: Int, task: EntityAIBase, controller: AIToggle){
			controller.setupTaskEntry(this, Pair(priority, task))
		}
	}
	
	var enabled by NotifyOnChange(false, ::onChange)
	
	private lateinit var taskList: EntityAITasks
	private lateinit var taskEntry: Pair<Int, EntityAIBase>
	
	private fun setupTaskEntry(taskList: EntityAITasks, taskEntry: Pair<Int, EntityAIBase>){
		this.taskList = taskList
		this.taskEntry = taskEntry
		
		if (enabled){
			onChange(true)
		}
	}
	
	private fun onChange(newValue: Boolean){
		if (!::taskList.isInitialized){
			return
		}
		
		if (newValue){
			val (priority, task) = taskEntry
			taskList.addTask(priority, task)
		}
		else{
			taskList.removeTask(taskEntry.second)
		}
	}
}
