package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.util.random.nextInt
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import net.minecraftforge.common.Tags

class BlockEndPowderOre(builder: BlockBuilder) : HeeBlock(builder) {
	override val drop
		get() = BlockDrop.Manual
	
	override val tags
		get() = listOf(Tags.Blocks.ORES)
	
	override fun getExpDrop(state: BlockState, world: IWorldReader, pos: BlockPos, fortune: Int, silktouch: Int): Int {
		return ((world as? World)?.rand ?: RANDOM).nextInt(1, 2)
	}
}
