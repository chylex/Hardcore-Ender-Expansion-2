package chylex.hee.game.block
import net.minecraft.block.Block
import net.minecraft.block.BlockStairs

class BlockStairsCustom(sourceBlock: Block) : BlockStairs(sourceBlock.defaultState){
	init{
		blockHardness *= sourceBlock.blockHardness * 0.75F
		useNeighborBrightness = true
	}
}
