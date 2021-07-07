package chylex.hee.game.item

import chylex.hee.game.entity.item.EntityItemNoBob
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ItemDragonEgg(block: Block, properties: Properties) : BlockItem(block, properties) {
	override fun hasCustomEntity(stack: ItemStack): Boolean {
		return true
	}
	
	override fun createEntity(world: World, replacee: Entity, stack: ItemStack): Entity {
		return EntityItemNoBob(world, stack, replacee)
	}
}
