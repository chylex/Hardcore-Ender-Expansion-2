package chylex.hee.game.block.dispenser
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.get
import chylex.hee.system.util.getState
import net.minecraft.block.BlockDispenser
import net.minecraft.dispenser.BehaviorDefaultDispenseItem
import net.minecraft.dispenser.IBehaviorDispenseItem
import net.minecraft.dispenser.IBlockSource
import net.minecraft.item.ItemStack

class DispenseWaterExtinguishIgneousPlate(private val originalBehavior: IBehaviorDispenseItem): BehaviorDefaultDispenseItem(){
	override fun dispenseStack(source: IBlockSource, stack: ItemStack): ItemStack{
		val world = source.world
		val facingPos = source.blockPos.offset(source.blockState[BlockDispenser.FACING])
		val facingState = facingPos.getState(world)
		
		if (facingState.block === ModBlocks.IGNEOUS_PLATE){
			return if (ModBlocks.IGNEOUS_PLATE.tryCoolPlate(world, facingPos, facingState))
				ItemStack(Items.BUCKET)
			else
				stack
		}
		
		return originalBehavior.dispense(source, stack)
	}
}
