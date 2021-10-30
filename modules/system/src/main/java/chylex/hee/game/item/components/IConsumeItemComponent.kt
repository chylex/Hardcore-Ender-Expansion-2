package chylex.hee.game.item.components

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.UseAction

interface IConsumeItemComponent {
	val action: UseAction
	fun getDuration(stack: ItemStack): Int
	
	fun tick(stack: ItemStack, entity: LivingEntity, tick: Int) {}
	fun finish(stack: ItemStack, entity: LivingEntity): ItemStack
}
