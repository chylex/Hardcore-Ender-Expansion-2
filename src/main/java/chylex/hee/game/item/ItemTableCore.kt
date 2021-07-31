package chylex.hee.game.item

import chylex.hee.game.block.BlockAbstractTableTile
import chylex.hee.game.block.BlockTableBase
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.setBlock
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World

class ItemTableCore(private val tableBlocks: Array<BlockAbstractTableTile<*>>, properties: Properties) : HeeItem(properties) {
	private companion object {
		private const val LANG_TOOLTIP_TIER = "item.tooltip.hee.table_core.tooltip"
	}
	
	override val localizationExtra
		get() = mapOf(LANG_TOOLTIP_TIER to "§7Minimum Tier: %s")
	
	override fun onItemUse(context: ItemUseContext): ActionResultType {
		val player = context.player ?: return FAIL
		val world = context.world
		val pos = context.pos
		
		val heldItem = player.getHeldItem(context.hand)
		
		if (!BlockEditor.canEdit(pos, player, heldItem)) {
			return FAIL
		}
		
		val block = pos.getBlock(world)
		
		if (block is BlockTableBase) {
			val table = tableBlocks.find { it.tier == block.tier } ?: return FAIL
			
			if (!world.isRemote) {
				pos.breakBlock(world, false)
				pos.setBlock(world, table)
			}
			
			heldItem.shrink(1)
			return SUCCESS
		}
		
		return PASS
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag) {
		lines.add(TranslationTextComponent(LANG_TOOLTIP_TIER, tableBlocks.minOf { it.tier }))
	}
}
