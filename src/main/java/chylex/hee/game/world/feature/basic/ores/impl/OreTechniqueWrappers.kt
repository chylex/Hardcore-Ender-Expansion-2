package chylex.hee.game.world.feature.basic.ores.impl

import chylex.hee.game.world.feature.basic.ores.IOreTechnique
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.system.facades.Facing6
import net.minecraft.util.math.BlockPos

fun IOreTechnique.withAdjacentAirCheck(checkDistance: Int = 1, chanceIfNoAir: Float = 0F) = object : IOreTechnique {
	override fun place(world: SegmentedWorld, pos: BlockPos, placer: IBlockPlacer): Boolean {
		for(distance in 1..checkDistance) {
			if (Facing6.any { isAirOrOutside(world, pos.offset(it, distance)) }) {
				return this@withAdjacentAirCheck.place(world, pos, placer)
			}
		}
		
		return world.rand.nextFloat() < chanceIfNoAir && this@withAdjacentAirCheck.place(world, pos, placer)
	}
	
	private fun isAirOrOutside(world: SegmentedWorld, pos: BlockPos): Boolean {
		return !world.isInside(pos) || world.isAir(pos)
	}
}
