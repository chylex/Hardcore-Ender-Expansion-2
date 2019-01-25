package chylex.hee.game.block
import net.minecraft.block.Block
import net.minecraft.block.BlockWall
import net.minecraft.block.state.IBlockState
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockWallCustom(sourceBlock: Block) : BlockWall(sourceBlock){
	override fun canPlaceTorchOnTop(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean{
		return true
	}
	
	override fun getSubBlocks(tab: CreativeTabs, items: NonNullList<ItemStack>){
		items.add(ItemStack(this))
	}
}
