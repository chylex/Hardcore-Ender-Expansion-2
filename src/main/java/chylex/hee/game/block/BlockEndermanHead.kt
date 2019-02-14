package chylex.hee.game.block
import chylex.hee.init.ModItems
import net.minecraft.block.BlockSkull
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.Random

class BlockEndermanHead : BlockSkull(){
	override fun getItem(world: World, pos: BlockPos, state: IBlockState): ItemStack{
		return ItemStack(ModItems.ENDERMAN_HEAD)
	}
	
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		if (!state.getValue(NODROP)){
			drops.add(ItemStack(ModItems.ENDERMAN_HEAD))
		}
	}
	
	override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item{
		return ModItems.ENDERMAN_HEAD
	}
}
