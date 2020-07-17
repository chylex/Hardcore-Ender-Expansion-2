package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.IBlockReader

abstract class BlockAbstractTable(builder: BlockBuilder, val tier: Int, val firstTier: Int) : BlockSimple(builder){
	init{
		require(tier in 1..3){ "[BlockAbstractTable] tier must be in the range 1..3" }
		require(firstTier <= tier){ "[BlockAbstractTable] firstTier cannot be larger than current tier" }
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: IBlockReader?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		lines.add(TranslationTextComponent("block.tooltip.hee.table.tier", tier))
	}
}
