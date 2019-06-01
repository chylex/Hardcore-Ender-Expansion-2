package chylex.hee.game.block.util

/**
 * Allows blocks to specify multiple tools that can harvest the block. Intended to be used in any code that checks [net.minecraft.block.Block.getHarvestTool].
 */
interface IBlockHarvestToolCheck{
	fun canHarvestUsing(toolClass: String, toolLevel: Int): Boolean
}
