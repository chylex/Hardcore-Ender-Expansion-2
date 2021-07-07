package chylex.hee.game.item.util

import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemModelsProperties
import net.minecraft.item.ItemStack
import net.minecraft.util.IItemProvider
import net.minecraft.util.ResourceLocation

abstract class ItemProperty(private val name: ResourceLocation) {
	abstract fun getValue(stack: ItemStack, entity: LivingEntity?): Float
	
	@Sided(Side.CLIENT)
	fun register(item: IItemProvider) {
		ItemModelsProperties.registerProperty(item.asItem(), name) { stack, _, entity -> getValue(stack, entity) }
	}
}

fun ItemProperty(name: ResourceLocation, valueGetter: (ItemStack) -> Float): ItemProperty {
	return object : ItemProperty(name) {
		override fun getValue(stack: ItemStack, entity: LivingEntity?): Float {
			return valueGetter(stack)
		}
	}
}

fun ItemProperty(name: ResourceLocation, valueGetter: (ItemStack, LivingEntity?) -> Float): ItemProperty {
	return object : ItemProperty(name) {
		override fun getValue(stack: ItemStack, entity: LivingEntity?): Float {
			return valueGetter(stack, entity)
		}
	}
}
