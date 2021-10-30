package chylex.hee.game.item.components

import net.minecraft.entity.Entity
import net.minecraft.entity.item.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

interface IItemEntityComponent {
	fun hasEntity(stack: ItemStack): Boolean {
		return true
	}
	
	fun createEntity(world: World, stack: ItemStack, replacee: Entity): ItemEntity
	
	companion object {
		fun fromConstructor(constructor: (World, ItemStack, Entity) -> ItemEntity) = object : IItemEntityComponent {
			override fun createEntity(world: World, stack: ItemStack, replacee: Entity): ItemEntity {
				return constructor(world, stack, replacee)
			}
		}
	}
}
