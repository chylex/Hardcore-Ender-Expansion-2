package chylex.hee.game.item.components

import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

fun interface ITickInInventoryComponent {
	fun tick(world: World, entity: Entity, stack: ItemStack, slot: Int, isSelected: Boolean)
}
