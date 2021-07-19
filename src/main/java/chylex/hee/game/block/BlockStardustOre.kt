package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.system.random.nextBiasedFloat
import chylex.hee.util.math.ceilToInt
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorldReader
import net.minecraft.world.World

class BlockStardustOre(builder: BlockBuilder) : BlockSimple(builder) {
	override val renderLayer
		get() = CUTOUT
	
	override fun getExpDrop(state: BlockState, world: IWorldReader, pos: BlockPos, fortune: Int, silktouch: Int): Int {
		return (((world as? World)?.rand ?: RANDOM).nextBiasedFloat(4F) * 6F).ceilToInt()
	}
}
