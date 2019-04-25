package chylex.hee.game.world.generation
import chylex.hee.game.world.generation.segments.SegmentSingleState
import chylex.hee.game.world.territory.TerritoryInstance
import net.minecraft.world.World

class TerritoryGenerationCache(private val world: World){
	private val definitelyTemporaryTerritoryWorldCache = mutableMapOf<TerritoryInstance, Pair<SegmentedWorld, TerritoryGenerationInfo>>() // TODO DEFINITELY TEMPORARY
	
	private fun constructInstance(instance: TerritoryInstance): Pair<SegmentedWorld, TerritoryGenerationInfo>{
		val territory = instance.territory
		val generator = territory.gen
		val rand = instance.createRandom(world)
		
		val segmentSize = generator.segmentSize
		val defaultBlock = generator.defaultBlock
		
		val world = SegmentedWorld(rand, territory.size, segmentSize){ SegmentSingleState(segmentSize, defaultBlock) }
		val info = generator.provide(world)
		
		return world to info
	}
	
	fun get(instance: TerritoryInstance): Pair<SegmentedWorld, TerritoryGenerationInfo>{
		return definitelyTemporaryTerritoryWorldCache.computeIfAbsent(instance, ::constructInstance)
	}
}
