package chylex.hee.game.world.generation.ore.impl

import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.ore.IOreTechnique
import chylex.hee.game.world.generation.structure.world.SegmentedWorld
import net.minecraft.util.math.BlockPos

object OreTechniqueSingle : IOreTechnique {
	override fun place(world: SegmentedWorld, pos: BlockPos, placer: IBlockPlacer): Boolean {
		return placer.place(world, pos)
	}
}
