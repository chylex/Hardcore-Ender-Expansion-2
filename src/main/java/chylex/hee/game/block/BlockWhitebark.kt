package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import net.minecraft.block.BlockState
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class BlockWhitebark(builder: BlockBuilder) : BlockSimple(builder) {
	override fun getFlammability(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 5
	}
	
	override fun getFireSpreadSpeed(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 5
	}
}
