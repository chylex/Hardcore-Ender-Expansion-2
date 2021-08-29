package chylex.hee.game.territory.generator

import chylex.hee.game.territory.system.ITerritoryGenerator
import chylex.hee.game.territory.system.TerritoryGenerationInfo
import chylex.hee.game.world.generation.structure.world.SegmentedWorld
import chylex.hee.util.math.Size

object Generator_EnderCity : ITerritoryGenerator {
	override val segmentSize = Size(32, 8, 32)
	
	override fun provide(world: SegmentedWorld): TerritoryGenerationInfo {
		TODO("not implemented")
	}
}
