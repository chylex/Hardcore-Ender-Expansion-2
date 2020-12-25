package chylex.hee.game.world

import chylex.hee.game.world.generation.TerritoryGenerationCache
import chylex.hee.game.world.generation.TerritoryGenerationInfo
import chylex.hee.game.world.math.Transform
import chylex.hee.game.world.territory.TerritoryInstance
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityClassification
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.Biome.SpawnListEntry
import net.minecraft.world.biome.BiomeManager
import net.minecraft.world.biome.provider.BiomeProvider
import net.minecraft.world.chunk.IChunk
import net.minecraft.world.gen.ChunkGenerator
import net.minecraft.world.gen.EndGenerationSettings
import net.minecraft.world.gen.Heightmap.Type
import net.minecraft.world.gen.WorldGenRegion
import net.minecraft.world.gen.feature.IFeatureConfig
import net.minecraft.world.gen.feature.structure.Structure
import net.minecraft.world.gen.feature.template.TemplateManager

class ChunkGeneratorEndCustom(world: World, biomeProvider: BiomeProvider, settings: EndGenerationSettings) : ChunkGenerator<EndGenerationSettings>(world, biomeProvider, settings) {
	private val territoryCache = TerritoryGenerationCache(world)
	
	// Instances
	
	private fun getInstance(chunkX: Int, chunkZ: Int): TerritoryInstance? {
		return TerritoryInstance.fromPos((chunkX * 16) + 8, (chunkZ * 16) + 8)
	}
	
	private fun getInternalOffset(chunkX: Int, chunkZ: Int, instance: TerritoryInstance): BlockPos {
		val (startChunkX, startChunkZ) = instance.topLeftChunk
		val internalOffsetX = (chunkX - startChunkX) * 16
		val internalOffsetZ = (chunkZ - startChunkZ) * 16
		
		return Pos(internalOffsetX, 0, internalOffsetZ)
	}
	
	private fun setState(chunk: IChunk, x: Int, y: Int, z: Int, state: BlockState) {
		chunk.setBlockState(Pos(x, y, z), state, false)
	}
	
	fun getGenerationInfo(instance: TerritoryInstance): TerritoryGenerationInfo {
		return territoryCache.get(instance).second
	}
	
	// Generation & population
	
	override fun makeBase(world: IWorld, chunk: IChunk) {}
	
	override fun generateSurface(region: WorldGenRegion, chunk: IChunk) {
		val (chunkX, chunkZ) = chunk.pos
		
		val instance = getInstance(chunkX, chunkZ) ?: return
		val territory = instance.territory
		
		val defaultState = territory.gen.defaultBlock.defaultState
		
		if (instance.generatesChunk(chunkX, chunkZ)) {
			val constructed = territoryCache.get(instance).first
			val height = constructed.worldSize.y
			
			val bottomOffset = territory.height.first
			val internalOffset = getInternalOffset(chunkX, chunkZ, instance)
			
			for(blockX in 0..15) for(blockZ in 0..15) {
				for(blockY in 0 until bottomOffset) {
					setState(chunk, blockX, blockY, blockZ, defaultState)
				}
				
				for(blockY in height until 256) {
					setState(chunk, blockX, blockY, blockZ, defaultState)
				}
			}
			
			for(blockY in 0 until height) for(blockX in 0..15) for(blockZ in 0..15) {
				setState(chunk, blockX, bottomOffset + blockY, blockZ, constructed.getState(internalOffset.add(blockX, blockY, blockZ)))
			}
		}
		else {
			for(blockY in 0 until 256) for(blockX in 0..15) for(blockZ in 0..15) {
				setState(chunk, blockX, blockY, blockZ, defaultState)
			}
		}
	}
	
	override fun decorate(region: WorldGenRegion) {
		val chunkX = region.mainChunkX
		val chunkZ = region.mainChunkZ
		
		val instance = getInstance(chunkX, chunkZ)?.takeIf { it.generatesChunk(chunkX, chunkZ) } ?: return
		val constructed = territoryCache.get(instance).first
		
		val startOffset = Pos(chunkX * 16, instance.territory.height.first, chunkZ * 16)
		val internalOffset = getInternalOffset(chunkX, chunkZ, instance)
		
		for((pos, trigger) in constructed.getTriggers()) {
			val blockOffset = pos.subtract(internalOffset)
			
			if (blockOffset.x in 0..15 && blockOffset.z in 0..15) {
				trigger.realize(region, startOffset.add(blockOffset), Transform.NONE)
			}
		}
	}
	
	override fun getPossibleCreatures(type: EntityClassification, pos: BlockPos): List<SpawnListEntry> {
		return emptyList() // TODO could be a good idea to use this instead of a custom spawner
	}
	
	override fun getGroundHeight(): Int {
		return 128
	}
	
	// Neutralization
	
	override fun func_222529_a(x: Int, z: Int, heightmapType: Type): Int { // RENAME getHeightAt
		return 256 // return a position outside the world, nothing should be calling this
	}
	
	override fun hasStructure(biome: Biome, structure: Structure<out IFeatureConfig>): Boolean {
		return false
	}
	
	override fun findNearestStructure(world: World, name: String, pos: BlockPos, radius: Int, skipExistingChunks: Boolean): BlockPos? {
		return null
	}
	
	override fun generateStructures(biomes: BiomeManager, chunk: IChunk, generator: ChunkGenerator<*>, templates: TemplateManager) {}
	override fun generateStructureStarts(world: IWorld, chunk: IChunk) {}
}
