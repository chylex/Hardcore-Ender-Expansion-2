package chylex.hee.game.world.feature
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.registries.ForgeRegistries
import java.util.Random

object OverworldFeatures{
	fun register(){
		// UPDATE GameRegistry.registerWorldGenerator(DispersedClusterGenerator, Int.MAX_VALUE)
		// UPDATE GameRegistry.registerWorldGenerator(EnergyShrineGenerator, Int.MAX_VALUE - 1)
		// UPDATE GameRegistry.registerWorldGenerator(StrongholdGenerator, Int.MAX_VALUE - 2)
	}
	
	fun setupVanillaOverrides(){
		for(biome in ForgeRegistries.BIOMES){
			biome.structures.remove(Feature.STRONGHOLD)
		}
	}
	
	fun findStartChunkInGrid(chunksInGrid: Int, chunkX: Int, chunkZ: Int): ChunkPos{
		val normalizedX = if (chunkX < 0) chunkX - (chunksInGrid - 1) else chunkX
		val normalizedZ = if (chunkZ < 0) chunkZ - (chunksInGrid - 1) else chunkZ
		
		return ChunkPos(
			chunksInGrid * (normalizedX / chunksInGrid),
			chunksInGrid * (normalizedZ / chunksInGrid)
		)
	}
	
	fun preloadChunks(world: World, chunkX: Int, chunkZ: Int, radiusX: Int, radiusZ: Int){
		for(offsetX in -radiusX..radiusX){
			for(offsetZ in -radiusZ..radiusZ){
				world.getChunk(chunkX + offsetX, chunkZ + offsetZ) // UPDATE shitty hack to force nearby structures to gen first
			}
		}
	}
	
	// UPDATE
	interface IOverworldFeature{
		fun generate(world: ServerWorld, chunkX: Int, chunkZ: Int, rand: Random)
	}
}
