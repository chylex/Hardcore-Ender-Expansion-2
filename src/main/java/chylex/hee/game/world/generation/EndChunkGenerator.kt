package chylex.hee.game.world.generation

import chylex.hee.game.Resource
import chylex.hee.game.territory.system.TerritoryGenerationInfo
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.world.generation.util.TerritoryGenerationCache
import chylex.hee.game.world.util.Transform
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.Pos
import chylex.hee.util.math.component1
import chylex.hee.util.math.component2
import com.mojang.serialization.Codec
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityClassification
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.DynamicRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryLookupCodec
import net.minecraft.world.Blockreader
import net.minecraft.world.IBlockReader
import net.minecraft.world.ISeedReader
import net.minecraft.world.IWorld
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeManager
import net.minecraft.world.biome.Biomes
import net.minecraft.world.biome.MobSpawnInfo.Spawners
import net.minecraft.world.biome.provider.SingleBiomeProvider
import net.minecraft.world.chunk.IChunk
import net.minecraft.world.gen.ChunkGenerator
import net.minecraft.world.gen.GenerationStage.Carving
import net.minecraft.world.gen.Heightmap.Type
import net.minecraft.world.gen.WorldGenRegion
import net.minecraft.world.gen.feature.structure.StructureManager
import net.minecraft.world.gen.feature.template.TemplateManager
import net.minecraft.world.gen.settings.DimensionStructuresSettings

class EndChunkGenerator(private val biomeRegistry: Registry<Biome>) : ChunkGenerator(SingleBiomeProvider(biomeRegistry.getOrThrow(Biomes.THE_END)), DimensionStructuresSettings(false)) {
	companion object {
		private val CODEC: Codec<EndChunkGenerator> = RegistryLookupCodec.getLookUpCodec(Registry.BIOME_KEY).xmap(::EndChunkGenerator, EndChunkGenerator::biomeRegistry).codec()
		
		fun registerCodec() {
			Registry.register(Registry.CHUNK_GENERATOR_CODEC, Resource.Custom("end"), CODEC)
		}
	}
	
	private lateinit var territoryCache: TerritoryGenerationCache
	
	fun setSeed(seed: Long) {
		check(!::territoryCache.isInitialized) { "[ChunkGeneratorEndCustom] cannot set seed twice" }
		territoryCache = TerritoryGenerationCache(seed)
	}
	
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
	
	override fun func_230352_b_(world: IWorld, structureManager: StructureManager, chunk: IChunk) {} // RENAME noise
	
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
			
			for (blockX in 0..15) for (blockZ in 0..15) {
				for (blockY in 0 until bottomOffset) {
					setState(chunk, blockX, blockY, blockZ, defaultState)
				}
				
				for (blockY in height until 256) {
					setState(chunk, blockX, blockY, blockZ, defaultState)
				}
			}
			
			for (blockY in 0 until height) for (blockX in 0..15) for (blockZ in 0..15) {
				setState(chunk, blockX, bottomOffset + blockY, blockZ, constructed.getState(internalOffset.add(blockX, blockY, blockZ)))
			}
		}
		else {
			for (blockY in 0 until 256) for (blockX in 0..15) for (blockZ in 0..15) {
				setState(chunk, blockX, blockY, blockZ, defaultState)
			}
		}
	}
	
	override fun func_230350_a_(seed: Long, biomeManager: BiomeManager, chunk: IChunk, carving: Carving) {} // RENAME carvers
	
	override fun func_230351_a_(region: WorldGenRegion, structureManager: StructureManager) { // RENAME decorate
		val chunkX = region.mainChunkX
		val chunkZ = region.mainChunkZ
		
		val instance = getInstance(chunkX, chunkZ)?.takeIf { it.generatesChunk(chunkX, chunkZ) } ?: return
		val constructed = territoryCache.get(instance).first
		
		val startOffset = Pos(chunkX * 16, instance.territory.height.first, chunkZ * 16)
		val internalOffset = getInternalOffset(chunkX, chunkZ, instance)
		
		for ((pos, trigger) in constructed.getTriggers()) {
			val blockOffset = pos.subtract(internalOffset)
			
			if (blockOffset.x in 0..15 && blockOffset.z in 0..15) {
				trigger.realize(region, startOffset.add(blockOffset), Transform.NONE)
			}
		}
	}
	
	override fun func_230353_a_(biome: Biome, structureManager: StructureManager, type: EntityClassification, pos: BlockPos): MutableList<Spawners> { // RENAME getPossibleCreatures
		return mutableListOf() // TODO could be a good idea to use this instead of a custom spawner
	}
	
	override fun getGroundHeight(): Int {
		return 128
	}
	
	// Properties
	
	override fun func_230347_a_(): Codec<out ChunkGenerator> {
		return CODEC
	}
	
	@Sided(Side.CLIENT)
	override fun func_230349_a_(seed: Long): ChunkGenerator {
		return EndChunkGenerator(biomeRegistry).apply { setSeed(seed) }
	}
	
	override fun getHeight(x: Int, z: Int, heightmapType: Type): Int {
		return 256 // return a position outside the world, nothing should be calling this
	}
	
	override fun func_230348_a_(x: Int, z: Int): IBlockReader {
		return Blockreader(emptyArray())
	}
	
	override fun func_242707_a(registries: DynamicRegistries, structureManager: StructureManager, chunk: IChunk, templateManager: TemplateManager, seed: Long) {} // RENAME generateStructureStarts
	override fun func_235953_a_(p_235953_1_: ISeedReader, p_235953_2_: StructureManager, p_235953_3_: IChunk) {} // RENAME generateStructureReferences
}
