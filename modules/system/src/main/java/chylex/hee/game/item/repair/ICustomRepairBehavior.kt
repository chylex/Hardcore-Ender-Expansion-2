package chylex.hee.game.item.repair

import chylex.hee.game.item.interfaces.IItemInterface

fun interface ICustomRepairBehavior : IItemInterface {
	fun onRepairUpdate(instance: RepairInstance)
}
