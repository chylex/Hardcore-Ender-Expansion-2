package chylex.hee.game.world.territory.generators
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.territory.ITerritoryGenerator
import chylex.hee.game.world.util.Size
import java.util.Random

object Generator_TheHub : ITerritoryGenerator{
	override val segmentSize = Size(32, 32, 32)
	
	override fun provide(rand: Random, world: SegmentedWorld){
	}
}
