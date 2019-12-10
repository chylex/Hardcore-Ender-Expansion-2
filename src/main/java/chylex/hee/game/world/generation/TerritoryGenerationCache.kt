package chylex.hee.game.world.generation
import chylex.hee.game.world.generation.segments.SegmentSingleState
import chylex.hee.game.world.territory.TerritoryInstance
import net.minecraft.world.World

class TerritoryGenerationCache(private val world: World){
	private val definitelyTemporaryTerritoryWorldCache = mutableMapOf<TerritoryInstance, Pair<SegmentedWorld, TerritoryGenerationInfo>>() // TODO DEFINITELY TEMPORARY
	
	private fun constructInstance(instance: TerritoryInstance): Pair<SegmentedWorld, TerritoryGenerationInfo>{
		val territory = instance.territory
		val generator = territory.gen
		val rand = instance.createRandom(world.seed)
		
		val worldSize = territory.size
		val segmentSize = generator.segmentSize
		val defaultBlock = generator.defaultBlock
		
		require(worldSize.x % 16 == 0 && worldSize.z % 16 == 0){ "territory world size must be chunk-aligned" }
		
		val world = SegmentedWorld(rand, worldSize, segmentSize){ SegmentSingleState(segmentSize, defaultBlock) }
		val info = generator.provide(world)
		
		return world to info
	}
	
	fun get(instance: TerritoryInstance): Pair<SegmentedWorld, TerritoryGenerationInfo>{
		return definitelyTemporaryTerritoryWorldCache.getOrPut(instance){ constructInstance(instance) }
	}
}
