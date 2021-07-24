package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockStateModels
import net.minecraft.block.BlockState
import net.minecraft.block.RotatedPillarBlock
import net.minecraft.tags.BlockTags
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class BlockWhitebarkLog(builder: BlockBuilder) : RotatedPillarBlock(builder.p), IHeeBlock {
	override val model
		get() = BlockStateModels.Log
	
	override val tags
		get() = listOf(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN)
	
	override fun getFlammability(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 5
	}
	
	override fun getFireSpreadSpeed(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 5
	}
}
