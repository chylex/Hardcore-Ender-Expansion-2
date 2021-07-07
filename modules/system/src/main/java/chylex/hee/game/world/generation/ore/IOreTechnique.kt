package chylex.hee.game.world.generation.ore

import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.structure.world.SegmentedWorld
import net.minecraft.util.math.BlockPos

interface IOreTechnique {
	fun place(world: SegmentedWorld, pos: BlockPos, placer: IBlockPlacer): Boolean
}
