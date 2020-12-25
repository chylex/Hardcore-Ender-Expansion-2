package chylex.hee.game.entity.living.ai

import chylex.hee.system.delegate.NotifyOnChange
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.goal.GoalSelector

class AIToggle {
	private class Entry(private val taskList: GoalSelector, private val priority: Int, private val instance: Goal) {
		fun update(newValue: Boolean) {
			if (newValue) {
				taskList.addGoal(priority, instance)
			}
			else {
				taskList.removeGoal(instance)
			}
		}
		
		fun conflicts(other: Entry): Boolean {
			return taskList === other.taskList && (priority == other.priority || instance === other.instance)
		}
		
		override fun toString(): String {
			return "$priority, ${instance::class.java.simpleName}"
		}
	}
	
	companion object {
		fun GoalSelector.addGoal(priority: Int, task: Goal, controller: AIToggle) {
			controller.addEntry(Entry(this, priority, task))
		}
	}
	
	// Instance
	
	var enabled by NotifyOnChange(false, ::onChange)
	
	private val entries = ArrayList<Entry>(1)
	
	private fun addEntry(newEntry: Entry) {
		val conflict = entries.find(newEntry::conflicts)
		
		if (conflict != null) {
			throw UnsupportedOperationException("task ($newEntry) is conflicting with ($conflict) in AI toggle group")
		}
		
		entries.add(newEntry)
		
		if (enabled) {
			newEntry.update(true)
		}
	}
	
	private fun onChange(newValue: Boolean) {
		for(entry in entries) {
			entry.update(newValue)
		}
	}
}
