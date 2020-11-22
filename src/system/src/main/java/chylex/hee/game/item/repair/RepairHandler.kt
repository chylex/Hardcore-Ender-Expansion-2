package chylex.hee.game.item.repair
import chylex.hee.HEE
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import net.minecraft.item.ItemStack
import net.minecraft.util.text.StringTextComponent
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
			
			if (instance.repaired.isEmpty || (instance.experienceCost > MAX_EXPERIENCE_COST && e.player?.abilities?.isCreativeMode == false)){
				e.output = ItemStack.EMPTY
				e.cost = 0
			}
			else{
				val output = instance.repaired.also { it.repairCost = instance.repairCost }
				
				val repairName = e.name
				var changedName = false
				
				if (repairName.isNullOrBlank()){
					if (target.hasDisplayName()){
						changedName = true
						output.clearCustomName()
					}
				}
				else if (repairName != target.displayName.string){
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
