package chylex.hee.game.block
import net.minecraft.block.Block
import net.minecraft.block.BlockStairs

class BlockStairsCustom(sourceBlock: Block) : BlockStairs(sourceBlock.defaultState){
	init{
		blockHardness *= 0.75F // UPDATE: Verify the assumption that (BlockStairs.blockHardness = sourceBlock.blockHardness) still applies
		useNeighborBrightness = true
	}
}
