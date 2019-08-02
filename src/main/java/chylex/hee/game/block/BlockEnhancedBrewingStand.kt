package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class BlockEnhancedBrewingStand(builder: BlockBuilder) : BlockBrewingStandOverride(builder){
	override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item{
		return Item.getItemFromBlock(this)
	}
	
	override fun getItem(world: World, pos: BlockPos, state: IBlockState): ItemStack{
		return ItemStack(this)
	}
}
