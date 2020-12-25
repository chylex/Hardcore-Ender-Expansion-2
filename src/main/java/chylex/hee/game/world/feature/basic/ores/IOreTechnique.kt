package chylex.hee.game.world.feature.basic.ores

import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.SegmentedWorld
import net.minecraft.util.math.BlockPos

interface IOreTechnique {
	fun place(world: SegmentedWorld, pos: BlockPos, placer: IBlockPlacer): Boolean
}
