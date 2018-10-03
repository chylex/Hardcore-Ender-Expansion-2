package chylex.hee.game.item
import chylex.hee.game.item.base.ItemBaseInfusable
import net.minecraft.block.Block
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemInfusedTNT(sourceBlock: Block) : ItemBlock(sourceBlock){
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		ItemBaseInfusable.onAddInformation(stack, lines)
	}
	
	@SideOnly(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean{
		return super.hasEffect(stack) || ItemBaseInfusable.onHasEffect(stack)
	}
}
