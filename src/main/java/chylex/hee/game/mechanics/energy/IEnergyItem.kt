package chylex.hee.game.mechanics.energy

import chylex.hee.game.item.interfaces.IItemInterface
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import org.apache.commons.lang3.math.Fraction

interface IEnergyItem : IItemInterface {
	fun getEnergyCapacity(stack: ItemStack): Units
	fun getEnergyPerUse(stack: ItemStack): Fraction
	
	fun hasAnyEnergy(stack: ItemStack): Boolean
	fun hasMaximumEnergy(stack: ItemStack): Boolean
	
	fun chargeUnit(stack: ItemStack): Boolean
	fun useUnit(entity: Entity, stack: ItemStack): Boolean
	
	fun getChargeLevel(stack: ItemStack): IEnergyQuantity
	fun setChargeLevel(stack: ItemStack, level: IEnergyQuantity)
	fun setChargePercentage(stack: ItemStack, percentage: Float)
}
