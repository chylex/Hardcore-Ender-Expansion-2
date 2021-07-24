package chylex.hee.game.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.tags.BlockTags
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class BlockFlammableSlab(fullBlock: Block) : BlockSlabCustom(fullBlock) {
	override val tags
		get() = super.tags + BlockTags.WOODEN_SLABS
	
	override fun getFlammability(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 20
	}
	
	override fun getFireSpreadSpeed(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 5
	}
}
