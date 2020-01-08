package chylex.hee.game.item
import chylex.hee.game.entity.item.EntityItemNoBob
import chylex.hee.system.migration.vanilla.ItemBlock
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ItemDragonEgg(block: Block, properties: Properties) : ItemBlock(block, properties){
	override fun hasCustomEntity(stack: ItemStack): Boolean{
		return true
	}
	
	override fun createEntity(world: World, replacee: Entity, stack: ItemStack): Entity{
		return EntityItemNoBob(world, stack, replacee)
	}
}
