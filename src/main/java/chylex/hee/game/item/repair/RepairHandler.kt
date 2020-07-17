package chylex.hee.game.item.repair
import chylex.hee.HEE
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraft.item.ItemStack
import net.minecraftforge.event.AnvilUpdateEvent

@SubscribeAllEvents(modid = HEE.ID)
object RepairHandler{
	const val MAX_EXPERIENCE_COST = 39
	
	@SubscribeEvent
	fun onAnvilUpdate(e: AnvilUpdateEvent){
		val target = e.left
		val ingredient = e.right
		
		val item = target.item
		
		if (item is ICustomRepairBehavior && item.getIsRepairable(target, ingredient)){
			val instance = RepairInstance(target, ingredient).apply(item::onRepairUpdate)
			
			if (instance.repaired.isEmpty || instance.experienceCost > MAX_EXPERIENCE_COST){ // POLISH check if player is in creative for xp limit, but I can't even do that with the event... replace all this shit with ASM
				e.output = ItemStack.EMPTY
				e.cost = 0
			}
			else{
				e.output = instance.repaired.also { it.repairCost = instance.repairCost } // POLISH cannot repair & change name at the same time, but CBA
				e.cost = instance.experienceCost
				e.materialCost = instance.ingredientCost
			}
		}
	}
}
