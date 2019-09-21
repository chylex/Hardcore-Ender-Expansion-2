package chylex.hee.game.block.dispenser
import chylex.hee.system.util.isNotEmpty
import net.minecraft.dispenser.IBlockSource
import net.minecraft.init.Bootstrap.BehaviorDispenseOptional
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

object DispenseEndermanHead : BehaviorDispenseOptional(){
	override fun dispenseStack(source: IBlockSource, stack: ItemStack): ItemStack{
		successful = ItemArmor.dispenseArmor(source, stack).isNotEmpty
		return stack
	}
}
