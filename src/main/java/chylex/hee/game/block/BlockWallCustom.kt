package chylex.hee.game.block
import net.minecraft.block.Block
import net.minecraft.block.BlockWall
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

class BlockWallCustom(sourceBlock: Block) : BlockWall(sourceBlock){
	override fun getSubBlocks(tab: CreativeTabs, items: NonNullList<ItemStack>){
		items.add(ItemStack(this))
	}
}
