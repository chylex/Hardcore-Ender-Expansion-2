package chylex.hee.game.item

import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.ItemBlock
import net.minecraft.block.Block
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World

class ItemInfusedTNT(block: Block, properties: Properties) : ItemBlock(block, properties), IInfusableItem {
	override fun canApplyInfusion(infusion: Infusion): Boolean {
		return ItemAbstractInfusable.onCanApplyInfusion(this, infusion)
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag) {
		super.addInformation(stack, world, lines, flags)
		ItemAbstractInfusable.onAddInformation(stack, lines)
	}
	
	@Sided(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean {
		return super.hasEffect(stack) || ItemAbstractInfusable.onHasEffect(stack)
	}
}
