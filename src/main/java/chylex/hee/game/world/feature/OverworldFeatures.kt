package chylex.hee.game.world.feature
import chylex.hee.HEE
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.basic.DispersedClusterGenerator
import chylex.hee.game.world.feature.energyshrine.EnergyShrineGenerator
import chylex.hee.game.world.feature.stronghold.StrongholdGenerator
import chylex.hee.game.world.xz
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.forge.named
import net.minecraft.util.SharedSeedRandom
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.ISeedReader
import net.minecraft.world.World
import net.minecraft.world.biome.Biome.Category.NETHER
import net.minecraft.world.biome.Biome.Category.THEEND
import net.minecraft.world.gen.ChunkGenerator
import net.minecraft.world.gen.GenerationStage.Decoration.TOP_LAYER_MODIFICATION
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.IFeatureConfig.NO_FEATURE_CONFIG
import net.minecraft.world.gen.feature.NoFeatureConfig
import net.minecraft.world.gen.feature.structure.Structure.STRONGHOLD
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.world.BiomeLoadingEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.registries.ForgeRegistries
import java.util.Random

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object OverworldFeatures{
	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun onRegister(e: RegistryEvent.Register<Feature<*>>){
		with(e.registry){
			register(DispersedClusterGenerator named "dispersed_clusters")
			register(EnergyShrineGenerator named "energy_shrine")
			register(StrongholdGenerator named "stronghold")
		}
	}
	
	@SubscribeEvent
	fun onBiomeLoading(e: BiomeLoadingEvent){
		if (e.category == NETHER || e.category == THEEND){
			return
		}
		
		with(e.generation){
			withFeature(TOP_LAYER_MODIFICATION, DispersedClusterGenerator.feature)
			withFeature(TOP_LAYER_MODIFICATION, EnergyShrineGenerator.feature)
			withFeature(TOP_LAYER_MODIFICATION, StrongholdGenerator.feature)
		}
	}
	
	fun setupVanillaOverrides(){
		for(biome in ForgeRegistries.BIOMES){
			biome.generationSettings.structures.removeIf { it.get().field_236268_b_ == STRONGHOLD }
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
	
	abstract class OverworldFeature : Feature<NoFeatureConfig>(NoFeatureConfig.field_236558_a_){
		val feature
			get() = ConfiguredFeature(this, NO_FEATURE_CONFIG)
		
		override fun generate(world: ISeedReader, generator: ChunkGenerator, rand: Random, pos: BlockPos, config: NoFeatureConfig): Boolean{
			return place(world, rand, pos, pos.x shr 4, pos.z shr 4)
		}
		
		protected abstract fun place(world: ISeedReader, rand: Random, pos: BlockPos, chunkX: Int, chunkZ: Int): Boolean
	}
	
	abstract class GeneratorTriggerBase : ITriggerHandler{
		final override fun check(world: World): Boolean{
			return !world.isRemote
		}
		
		final override fun update(entity: EntityTechnicalTrigger){
			val world = entity.world as ServerWorld
			
			if (world.dimensionKey != World.OVERWORLD){
				entity.remove()
				return
			}
			
			val pos = Pos(entity)
			val xz = pos.xz
			
			// TODO wait until either nearby chunks are loaded or a player gets close enough
			
			place(world, SharedSeedRandom().apply { setLargeFeatureSeed(world.seed, xz.chunkX, xz.chunkZ) }, pos)
			entity.remove()
		}
		
		final override fun nextTimer(rand: Random): Int{
			return 10
		}
		
		protected abstract fun place(world: ServerWorld, rand: Random, pos: BlockPos)
	}
}
