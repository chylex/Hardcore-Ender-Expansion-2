package chylex.hee.game.item

import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemModelsProperties
import net.minecraft.item.ItemStack
import net.minecraft.util.IItemProvider

abstract class ItemProperty(name: String) {
	val location = Resource.Custom(name)
	
	abstract fun getValue(stack: ItemStack, entity: LivingEntity?): Float
	
	@Sided(Side.CLIENT)
	fun register(item: IItemProvider) {
		ItemModelsProperties.registerProperty(item.asItem(), location) { stack, _, entity -> getValue(stack, entity) }
	}
}

fun ItemProperty(name: String, valueGetter: (ItemStack) -> Float): ItemProperty {
	return object : ItemProperty(name) {
		override fun getValue(stack: ItemStack, entity: LivingEntity?): Float {
			return valueGetter(stack)
		}
	}
}

fun ItemProperty(name: String, valueGetter: (ItemStack, LivingEntity?) -> Float): ItemProperty {
	return object : ItemProperty(name) {
		override fun getValue(stack: ItemStack, entity: LivingEntity?): Float {
			return valueGetter(stack, entity)
		}
	}
}
