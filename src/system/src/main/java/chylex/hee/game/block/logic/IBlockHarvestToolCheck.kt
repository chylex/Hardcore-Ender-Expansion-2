package chylex.hee.game.block.logic

import net.minecraftforge.common.ToolType

/**
 * Allows blocks to specify multiple tools that can harvest the block. Intended to be used in any code that checks [net.minecraft.block.Block.getHarvestTool].
 */
interface IBlockHarvestToolCheck {
	fun canHarvestUsing(toolClass: ToolType, toolLevel: Int): Boolean
}
