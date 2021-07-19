package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.init.ModBlocks
import net.minecraft.block.BlockState
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class BlockWhitebark(builder: BlockBuilder) : HeeBlock(builder) {
	override val model
		get() = BlockModel.Cube(ModBlocks.WHITEBARK_LOG.location)
	
	override fun getFlammability(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 5
	}
	
	override fun getFireSpreadSpeed(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 5
	}
}
