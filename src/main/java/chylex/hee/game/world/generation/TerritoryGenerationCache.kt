package chylex.hee.game.world.generation
import chylex.hee.HEE
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
		
		val timeStart = System.currentTimeMillis()
		val generated = territory.generate(instance.createRandom(world.seed))
		val timeEnd = System.currentTimeMillis()
		
		HEE.log.info("[TerritoryGenerationCache] generated ${territory.name} in ${timeEnd - timeStart} ms")
		return generated
	}
	
	fun get(instance: TerritoryInstance): Pair<SegmentedWorld, TerritoryGenerationInfo>{
		return cache.getIfPresent(instance) ?: constructInstance(instance).also { cache.put(instance, it) }
	}
}
