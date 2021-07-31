package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.IBlockReader

abstract class BlockAbstractTable(builder: BlockBuilder, val tier: Int, val firstTier: Int) : HeeBlock(builder) {
	private companion object {
		private const val LANG_TOOLTIP_TIER = "block.tooltip.hee.table.tier"
	}
	
	override val localization
		get() = LocalizationStrategy.DeleteWords(toDelete = "Tier $tier")
	
	override val localizationExtra
		get() = mapOf(LANG_TOOLTIP_TIER to "§7Tier %s")
	
	override val renderLayer
		get() = CUTOUT
	
	init {
		require(tier in 1..3) { "[BlockAbstractTable] tier must be in the range 1..3" }
		require(firstTier <= tier) { "[BlockAbstractTable] firstTier cannot be larger than current tier" }
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: IBlockReader?, lines: MutableList<ITextComponent>, flags: ITooltipFlag) {
		lines.add(TranslationTextComponent(LANG_TOOLTIP_TIER, tier))
	}
}
