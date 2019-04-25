package chylex.hee.game.world
import chylex.hee.game.world.generation.TerritoryGenerationCache
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.storage.TerritoryGlobalStorage
import chylex.hee.system.util.Pos
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import net.minecraft.entity.EnumCreatureType
import net.minecraft.init.Biomes
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.Biome.SpawnListEntry
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.IChunkGenerator

class ChunkGeneratorEndCustom(private val world: World) : IChunkGenerator{
	private val territoryCache = TerritoryGenerationCache(world)
	
	// Instances
	
	private fun getInstance(chunkX: Int, chunkZ: Int): TerritoryInstance?{
		return TerritoryInstance.fromPos((chunkX * 16) + 8, (chunkZ * 16) + 8)?.takeIf { it.generatesChunk(chunkX, chunkZ) }
	}
	
	private fun getInternalOffset(chunkX: Int, chunkZ: Int, instance: TerritoryInstance): BlockPos{
		val (startChunkX, startChunkZ) = instance.topLeftChunk
		val internalOffsetX = (chunkX - startChunkX) * 16
		val internalOffsetZ = (chunkZ - startChunkZ) * 16
		
		return Pos(internalOffsetX, 0, internalOffsetZ)
	}
	
	fun initializeSpawnPoint(instance: TerritoryInstance){
		TerritoryGlobalStorage.get().forInstance(instance)?.initializeSpawnPoint(territoryCache.get(instance).second)
	}
	
	// Generation & population
	
	override fun generateChunk(chunkX: Int, chunkZ: Int): Chunk{
		return Chunk(world, primeChunk(chunkX, chunkZ), chunkX, chunkZ).apply {
			biomeArray.fill(Biome.getIdForBiome(Biomes.SKY).toByte())
			generateSkylightMap()
		}
	}
	
	private fun primeChunk(chunkX: Int, chunkZ: Int) = ChunkPrimer().apply {
		val instance = getInstance(chunkX, chunkZ) ?: return@apply
		val constructed = territoryCache.get(instance).first
		val height = constructed.worldSize.y
		
		val defaultState = instance.territory.gen.defaultBlock.defaultState
		val blockOffsetY = instance.territory.height.first
		val internalOffset = getInternalOffset(chunkX, chunkZ, instance)
		
		for(blockX in 0..15) for(blockZ in 0..15){
			for(blockY in 0 until blockOffsetY){
				setBlockState(blockX, blockY, blockZ, defaultState)
			}
			
			for(blockY in height until 256){
				setBlockState(blockX, blockY, blockZ, defaultState)
			}
		}
		
		for(blockY in 0 until height) for(blockX in 0..15) for(blockZ in 0..15){
			setBlockState(blockX, blockOffsetY + blockY, blockZ, constructed.getState(internalOffset.add(blockX, blockY, blockZ)))
		}
	}
	
	override fun populate(chunkX: Int, chunkZ: Int){ // TODO disable forge worldgen
		val instance = getInstance(chunkX, chunkZ) ?: return
		val constructed = territoryCache.get(instance).first
		
		val startOffset = Pos(chunkX * 16, instance.territory.height.first, chunkZ * 16)
		val internalOffset = getInternalOffset(chunkX, chunkZ, instance)
		
		for((pos, trigger) in constructed.getTriggers()){
			val blockOffset = pos.subtract(internalOffset)
			
			if (blockOffset.x in 0..15 && blockOffset.z in 0..15){
				trigger.realize(world, startOffset.add(blockOffset), Rotation.NONE)
			}
		}
	}
	
	override fun getPossibleCreatures(creatureType: EnumCreatureType, pos: BlockPos): List<SpawnListEntry>{
		return emptyList() // TODO could be a good idea to use this instead of a custom spawner
	}
	
	// Neutralization
	
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
