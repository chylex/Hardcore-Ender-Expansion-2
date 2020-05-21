package chylex.hee.game.world.generation
import chylex.hee.HEE
import chylex.hee.game.world.generation.segments.SegmentSingleState
import chylex.hee.game.world.territory.TerritoryInstance
import com.google.common.cache.CacheBuilder
import net.minecraft.world.World
import java.util.concurrent.TimeUnit

class TerritoryGenerationCache(private val world: World){
	private val cache = CacheBuilder
		.newBuilder()
		.initialCapacity(4)
		.maximumSize(24)
		.concurrencyLevel(2)
		.expireAfterAccess(5, TimeUnit.MINUTES)
		.build<TerritoryInstance, Pair<SegmentedWorld, TerritoryGenerationInfo>>()
	
	private fun constructInstance(instance: TerritoryInstance): Pair<SegmentedWorld, TerritoryGenerationInfo>{
		val territory = instance.territory
		val generator = territory.gen
		val rand = instance.createRandom(world.seed)
		
		val worldSize = territory.size
		val segmentSize = generator.segmentSize
		val defaultBlock = generator.defaultBlock
		
		require(worldSize.x % 16 == 0 && worldSize.z % 16 == 0){ "territory world size must be chunk-aligned" }
		
		val timeStart = System.currentTimeMillis()
		
		val world = SegmentedWorld(rand, worldSize, segmentSize){ SegmentSingleState(segmentSize, defaultBlock) }
		val info = generator.provide(world)
		
		val timeEnd = System.currentTimeMillis()
		HEE.log.info("[TerritoryGenerationCache] generated ${territory.name} in ${timeEnd - timeStart} ms")
		
		return world to info
	}
	
	fun get(instance: TerritoryInstance): Pair<SegmentedWorld, TerritoryGenerationInfo>{
		return cache.getIfPresent(instance) ?: constructInstance(instance).also { cache.put(instance, it) }
	}
}
