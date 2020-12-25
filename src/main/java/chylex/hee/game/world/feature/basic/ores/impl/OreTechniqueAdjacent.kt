package chylex.hee.game.world.feature.basic.ores.impl

import chylex.hee.game.world.feature.basic.ores.IOreTechnique
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.system.facades.Facing6
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import net.minecraft.util.math.BlockPos
import java.util.Random
import kotlin.math.abs

class OreTechniqueAdjacent(
	private val oresPerCluster: (Random) -> Int,
	private val allowDiagonals: Boolean,
) : IOreTechnique {
	override fun place(world: SegmentedWorld, pos: BlockPos, placer: IBlockPlacer): Boolean {
		val rand = world.rand
		val ores = oresPerCluster(rand).takeIf { it > 0 } ?: return true
		
		if (!placer.place(world, pos)) {
			return false
		}
		
		val generatedOres = mutableListOf(pos)
		
		repeat(ores - 1) {
			for(attempt in 1..5) {
				val next = pickAdjacent(rand, rand.nextItem(generatedOres))
				
				if (placer.place(world, next)) {
					generatedOres.add(next)
					break
				}
			}
		}
		
		return true
	}
	
	private fun pickAdjacent(rand: Random, pos: BlockPos): BlockPos {
		val facing = rand.nextItem(Facing6)
		
		if (allowDiagonals && rand.nextInt(3) != 0) {
			return pos.offset(facing).add(
				rand.nextInt(-1, 1) * (1 - abs(facing.xOffset)),
				rand.nextInt(-1, 1) * (1 - abs(facing.yOffset)),
				rand.nextInt(-1, 1) * (1 - abs(facing.zOffset))
			)
		}
		
		return pos.offset(facing)
	}
}
