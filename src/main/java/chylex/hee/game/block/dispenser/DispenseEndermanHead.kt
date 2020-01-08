package chylex.hee.game.block.dispenser
import chylex.hee.system.migration.vanilla.ItemArmor
import chylex.hee.system.util.isNotEmpty
import net.minecraft.dispenser.IBlockSource
import net.minecraft.dispenser.OptionalDispenseBehavior
import net.minecraft.item.ItemStack

object DispenseEndermanHead : OptionalDispenseBehavior(){
	override fun dispenseStack(source: IBlockSource, stack: ItemStack): ItemStack{
		successful = ItemArmor.dispenseArmor(source, stack).isNotEmpty
		return stack
	}
}
