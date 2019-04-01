package chylex.hee.game.world.feature
import chylex.hee.game.world.feature.basic.DispersedClusterGenerator
import chylex.hee.game.world.feature.stronghold.StrongholdGenerator
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.GameRegistry

object OverworldFeatures{
	fun register(){
		GameRegistry.registerWorldGenerator(DispersedClusterGenerator, Int.MAX_VALUE)
		GameRegistry.registerWorldGenerator(StrongholdGenerator, Int.MAX_VALUE - 1)
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
}
