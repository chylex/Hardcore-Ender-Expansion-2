package chylex.hee.game.item.repair

import chylex.hee.HEE
import chylex.hee.game.item.interfaces.getHeeInterface
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.event.AnvilUpdateEvent

@SubscribeAllEvents(modid = HEE.ID)
object RepairHandler {
	const val MAX_EXPERIENCE_COST = 39
	
	@SubscribeEvent
	fun onAnvilUpdate(e: AnvilUpdateEvent) {
		val target = e.left
		val ingredient = e.right
		
		val item = target.item
		val repairBehavior = item.getHeeInterface() ?: item as? ICustomRepairBehavior
		
		if (repairBehavior != null && item.getIsRepairable(target, ingredient)) {
			val instance = RepairInstance(target, ingredient).apply(repairBehavior::onRepairUpdate)
			
			if (instance.repaired.isEmpty || (instance.experienceCost > MAX_EXPERIENCE_COST && !e.player.let { it != null && it.abilities.isCreativeMode })) {
				e.isCanceled = true
			}
			else {
				val output = instance.repaired.also { it.repairCost = instance.repairCost }
				
				val repairName = e.name
				var changedName = false
				
				if (repairName.isNullOrBlank()) {
					if (target.hasDisplayName()) {
						changedName = true
						output.clearCustomName()
					}
				}
				else if (repairName != target.displayName.string) {
					changedName = true
					output.displayName = StringTextComponent(repairName)
				}
				
				e.output = output
				e.cost = instance.experienceCost + (if (changedName) 1 else 0)
				e.materialCost = instance.ingredientCost
			}
		}
	}
}
