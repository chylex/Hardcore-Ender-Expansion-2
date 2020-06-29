package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.nextBiasedFloat
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorldReader
import net.minecraft.world.World

class BlockStardustOre(builder: BlockBuilder) : BlockSimple(builder){
	override fun getExpDrop(state: BlockState, world: IWorldReader, pos: BlockPos, fortune: Int, silktouch: Int): Int{
		return (((world as? World)?.rand ?: RANDOM).nextBiasedFloat(4F) * 6F).ceilToInt()
	}
}
