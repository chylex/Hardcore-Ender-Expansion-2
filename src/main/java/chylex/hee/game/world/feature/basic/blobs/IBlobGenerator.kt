package chylex.hee.game.world.feature.basic.blobs
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.Facing6
import chylex.hee.system.util.allInCenteredBoxMutable
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.square
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import java.util.Random

interface IBlobGenerator{
	val size: Size
	fun generate(world: SegmentedWorld, rand: Random)
	
	companion object{
		fun placeBlob(world: SegmentedWorld, center: BlockPos, radius: Double, block: Block = Blocks.END_STONE): Boolean{
			val offset = radius.ceilToInt()
			
			if (!world.isInside(center) || Facing6.any { !world.isInside(center.offset(it, offset)) }){
				return false
			}
			
			val radiusSq = square(radius + 0.5)
			
			for(pos in center.allInCenteredBoxMutable(offset, offset, offset)){
				if (pos.distanceSqTo(center) <= radiusSq){
					world.setBlock(pos, block)
				}
			}
			
			return true
		}
	}
}
