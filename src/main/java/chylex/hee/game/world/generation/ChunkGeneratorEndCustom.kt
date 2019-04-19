package chylex.hee.game.world.generation
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.system.util.Pos
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import net.minecraft.entity.EnumCreatureType
import net.minecraft.init.Biomes
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.Biome.SpawnListEntry
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.IChunkGenerator

class ChunkGeneratorEndCustom(private val world: World) : IChunkGenerator{
	private val definitelyTemporaryTerritoryWorldCache = mutableMapOf<TerritoryInstance, SegmentedWorld>() // TODO DEFINITELY TEMPORARY
	
	override fun generateChunk(x: Int, z: Int): Chunk{
		return Chunk(world, primeChunk(x, z), x, z).apply {
			biomeArray.fill(Biome.getIdForBiome(Biomes.SKY).toByte())
			generateSkylightMap()
		}
	}
	
	private fun primeChunk(chunkX: Int, chunkZ: Int) = ChunkPrimer().apply {
		val instance = TerritoryInstance.fromPos((chunkX * 16) + 8, (chunkZ * 16) + 8)
		
		if (instance == null || !instance.generatesChunk(chunkX, chunkZ)){
			return@apply
		}
		
		val (startChunkX, startChunkZ) = instance.topLeftChunk
		
		val internalOffsetX = (chunkX - startChunkX) * 16
		val internalOffsetZ = (chunkZ - startChunkZ) * 16
		val blockOffsetY = instance.territory.height.first
		
		val world = definitelyTemporaryTerritoryWorldCache.computeIfAbsent(instance, ::constructWorld)
		
		for(blockY in 0..world.worldSize.maxY) for(blockX in 0..15) for(blockZ in 0..15){
			setBlockState(blockX, blockOffsetY + blockY, blockZ, world.getState(Pos(internalOffsetX + blockX, blockY, internalOffsetZ + blockZ)))
		}
	}
	
	private fun constructWorld(instance: TerritoryInstance): SegmentedWorld{
		val territory = instance.territory
		val generator = territory.gen
		
		return SegmentedWorld(territory.size, generator.segmentSize){
			generator.defaultSegment()
		}.also {
			generator.provide(instance.createRandom(world), it)
		}
	}
	
	override fun populate(x: Int, z: Int){} // TODO disable forge worldgen
	
	override fun getPossibleCreatures(creatureType: EnumCreatureType, pos: BlockPos): List<SpawnListEntry>{
		return emptyList() // TODO could be a good idea to use this instead of a custom spawner
	}
	
	override fun generateStructures(chunk: Chunk, x: Int, z: Int): Boolean{
		return false
	}
	
	override fun recreateStructures(chunk: Chunk, x: Int, z: Int){}
	
	override fun isInsideStructure(world: World, structureName: String, pos: BlockPos): Boolean{
		return false
	}
	
	override fun getNearestStructurePos(world: World, structureName: String, position: BlockPos, findUnexplored: Boolean): BlockPos?{
		return null
	}
}
