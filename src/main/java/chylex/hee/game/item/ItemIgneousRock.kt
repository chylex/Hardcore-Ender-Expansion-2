package chylex.hee.game.item
import chylex.hee.game.entity.item.EntityItemIgneousRock
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ItemIgneousRock(properties: Properties) : Item(properties){
	override fun getBurnTime(stack: ItemStack): Int{
		return 1300
	}
	
	override fun hasCustomEntity(stack: ItemStack): Boolean{
		return true
	}
	
	override fun createEntity(world: World, replacee: Entity, stack: ItemStack): Entity{
		return EntityItemIgneousRock(world, stack, replacee)
	}
}
