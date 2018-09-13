package chylex.hee.game.item
import net.minecraft.item.Item
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ItemIgneousRock : Item(){
	init{
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent
	fun onFuelBurnTime(e: FurnaceFuelBurnTimeEvent){
		if (e.itemStack.item == this){
			e.burnTime = 1300
		}
	}
}
