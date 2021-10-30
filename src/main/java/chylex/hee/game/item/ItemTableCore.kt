package chylex.hee.game.item

import chylex.hee.game.block.BlockAbstractTableTile
import chylex.hee.game.block.BlockTableBase
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.PlayerUseItemOnBlockComponent
import chylex.hee.game.item.components.StaticTooltipComponent
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.setBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World

class ItemTableCore(private val tableBlocks: Array<BlockAbstractTableTile<*>>) : HeeItemBuilder() {
	private companion object {
		private const val LANG_TOOLTIP_TIER = "item.tooltip.hee.table_core.tooltip"
	}
	
	init {
		localizationExtra[LANG_TOOLTIP_TIER] = "§7Minimum Tier: %s"
		
		components.tooltip.add(StaticTooltipComponent(TranslationTextComponent(LANG_TOOLTIP_TIER, tableBlocks.minOf { it.tier })))
		
		components.useOnBlock = object : PlayerUseItemOnBlockComponent() {
			override fun use(world: World, pos: BlockPos, player: PlayerEntity, heldItem: ItemStack, context: ItemUseContext): ActionResultType {
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
		}
	}
}
