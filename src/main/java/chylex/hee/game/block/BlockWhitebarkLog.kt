package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.system.migration.BlockRotatedPillar
import net.minecraft.block.BlockState
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class BlockWhitebarkLog(builder: BlockBuilder) : BlockRotatedPillar(builder.p) {
	override fun getFlammability(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 5
	}
	
	override fun getFireSpreadSpeed(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 5
	}
}
