package chylex.hee.game.block.dispenser
import net.minecraft.dispenser.IBlockSource
import net.minecraft.init.Bootstrap.BehaviorDispenseOptional
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

object DispenseEndermanHead : BehaviorDispenseOptional(){
	override fun dispenseStack(source: IBlockSource, stack: ItemStack): ItemStack{
		successful = true
		
		// TODO implement placement behavior if anyone even cares
		
		if (ItemArmor.dispenseArmor(source, stack).isEmpty){
			successful = false
		}
		
		return stack
	}
}
