package chylex.hee.game.item
import chylex.hee.game.entity.item.EntityItemNoBob
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ItemDragonEgg(sourceBlock: Block) : ItemBlock(sourceBlock){
	override fun hasCustomEntity(stack: ItemStack): Boolean{
		return true
	}
	
	override fun createEntity(world: World, replacee: Entity, stack: ItemStack): Entity{
		return EntityItemNoBob(world, stack, replacee)
	}
}
