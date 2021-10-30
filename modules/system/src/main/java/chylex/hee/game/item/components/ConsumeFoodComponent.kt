package chylex.hee.game.item.components

import net.minecraft.entity.LivingEntity
import net.minecraft.item.Food
import net.minecraft.item.ItemStack
import net.minecraft.item.UseAction

abstract class ConsumeFoodComponent(private val food: Food) : IConsumeItemComponent {
	override val action
		get() = UseAction.EAT
	
	override fun getDuration(stack: ItemStack): Int {
		return if (food.isFastEating) 16 else 32
	}
	
	override fun finish(stack: ItemStack, entity: LivingEntity): ItemStack {
		return entity.onFoodEaten(entity.world, stack)
	}
}
