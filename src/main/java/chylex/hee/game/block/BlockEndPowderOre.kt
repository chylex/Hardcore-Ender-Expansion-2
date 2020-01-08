package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.nextInt
import net.minecraft.block.BlockState
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorldReader
import net.minecraft.world.World

class BlockEndPowderOre(builder: BlockBuilder) : BlockSimple(builder){
	override fun getLootTable(): ResourceLocation{
		return Resource.Custom("blocks/end_powder_ore")
	}
	
	override fun getExpDrop(state: BlockState, world: IWorldReader, pos: BlockPos, fortune: Int, silktouch: Int): Int{
		return ((world as? World)?.rand ?: RANDOM).nextInt(1, 2)
	}
	
	// UPDATE override fun canSilkHarvest() = true
}
