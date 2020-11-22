package chylex.hee.game.block.dispenser
import chylex.hee.system.migration.ItemArmor
import net.minecraft.dispenser.IBlockSource
import net.minecraft.dispenser.OptionalDispenseBehavior
import net.minecraft.item.ItemStack

object DispenseEndermanHead : OptionalDispenseBehavior(){
	override fun dispenseStack(source: IBlockSource, stack: ItemStack): ItemStack{
		isSuccessful = ItemArmor.func_226626_a_(source, stack) // RENAME dispenseArmor
		return stack
	}
}
