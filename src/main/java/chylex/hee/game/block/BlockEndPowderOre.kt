package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.system.random.nextInt
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorldReader
import net.minecraft.world.World

class BlockEndPowderOre(builder: BlockBuilder) : BlockSimple(builder) {
	override fun getExpDrop(state: BlockState, world: IWorldReader, pos: BlockPos, fortune: Int, silktouch: Int): Int {
		return ((world as? World)?.rand ?: RANDOM).nextInt(1, 2)
	}
}
