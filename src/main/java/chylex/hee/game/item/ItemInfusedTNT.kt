package chylex.hee.game.item
import chylex.hee.game.item.base.IInfusableItem
import chylex.hee.game.item.base.ItemBaseInfusable
import chylex.hee.game.item.infusion.Infusion
import net.minecraft.block.Block
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemInfusedTNT(sourceBlock: Block) : ItemBlock(sourceBlock), IInfusableItem{
	override fun canApplyInfusion(infusion: Infusion): Boolean{
		return ItemBaseInfusable.onCanApplyInfusion(this, infusion)
	}
	
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
