package chylex.hee.game.block.dispenser

import net.minecraft.dispenser.IBlockSource
import net.minecraft.dispenser.OptionalDispenseBehavior
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack

object DispenseEndermanHead : OptionalDispenseBehavior() {
	override fun dispenseStack(source: IBlockSource, stack: ItemStack): ItemStack {
		isSuccessful = ArmorItem.func_226626_a_(source, stack) // RENAME dispenseArmor
		return stack
	}
}
