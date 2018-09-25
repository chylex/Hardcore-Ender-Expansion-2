package chylex.hee.game.item
import chylex.hee.game.entity.item.EntityItemIgneousRock
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ItemIgneousRock : Item(){
	init{
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent
	fun onFuelBurnTime(e: FurnaceFuelBurnTimeEvent){
		if (e.itemStack.item === this){
			e.burnTime = 1300
		}
	}
	
	override fun hasCustomEntity(stack: ItemStack): Boolean{
		return true
	}
	
	override fun createEntity(world: World, replacee: Entity, stack: ItemStack): Entity{
		return EntityItemIgneousRock(world, stack, replacee)
	}
}
