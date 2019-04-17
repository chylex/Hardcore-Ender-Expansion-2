package chylex.hee.game.world.territory
import chylex.hee.game.world.generation.ISegment
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.generation.segments.SegmentSingleState
import chylex.hee.game.world.util.Size
import net.minecraft.init.Blocks
import java.util.Random

interface ITerritoryGenerator{
	val segmentSize: Size
	
	@JvmDefault
	fun defaultSegment(): ISegment{
		return SegmentSingleState(segmentSize, Blocks.AIR)
	}
	
	fun provide(rand: Random, world: SegmentedWorld)
}
