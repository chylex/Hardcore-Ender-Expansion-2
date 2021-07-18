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
import chylex.hee.system.migration.supply
import net.minecraft.util.SharedSeedRandom
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.WorldGenRegistries
import net.minecraft.util.registry.WorldGenRegistries.CONFIGURED_FEATURE
import net.minecraft.world.DimensionType
import net.minecraft.world.ISeedReader
import net.minecraft.world.World
import net.minecraft.world.gen.ChunkGenerator
import net.minecraft.world.gen.GenerationStage.Decoration.TOP_LAYER_MODIFICATION
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.IFeatureConfig.NO_FEATURE_CONFIG
import net.minecraft.world.gen.feature.NoFeatureConfig
import net.minecraft.world.gen.feature.structure.StructureFeatures
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.world.BiomeLoadingEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import java.util.Random

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object OverworldFeatures {
	init {
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun onRegisterFeatures(e: RegistryEvent.Register<Feature<*>>) {
		with(e.registry) {
			register(DispersedClusterGenerator named "dispersed_clusters")
			register(EnergyShrineGenerator named "energy_shrine")
			register(StrongholdGenerator named "stronghold")
		}
	}
	
	fun registerConfiguredFeatures() {
		WorldGenRegistries.register(CONFIGURED_FEATURE, DispersedClusterGenerator.registryName!!, DispersedClusterGenerator.feature)
		WorldGenRegistries.register(CONFIGURED_FEATURE, EnergyShrineGenerator.registryName!!, EnergyShrineGenerator.feature)
		WorldGenRegistries.register(CONFIGURED_FEATURE, StrongholdGenerator.registryName!!, StrongholdGenerator.feature)
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	fun onBiomeLoading(e: BiomeLoadingEvent) {
		with (e.generation) {
			getFeatures(TOP_LAYER_MODIFICATION).addAll(arrayOf(
				supply(DispersedClusterGenerator.feature),
				supply(EnergyShrineGenerator.feature),
				supply(StrongholdGenerator.feature),
			))
			
			structures.removeAll {
				it.get() == StructureFeatures.STRONGHOLD
			}
		}
	}
	
	fun findStartChunkInGrid(chunksInGrid: Int, chunkX: Int, chunkZ: Int): ChunkPos {
		val normalizedX = if (chunkX < 0) chunkX - (chunksInGrid - 1) else chunkX
		val normalizedZ = if (chunkZ < 0) chunkZ - (chunksInGrid - 1) else chunkZ
		
		return ChunkPos(
			chunksInGrid * (normalizedX / chunksInGrid),
			chunksInGrid * (normalizedZ / chunksInGrid)
		)
	}
	
	fun preloadChunks(world: World, chunkX: Int, chunkZ: Int, radiusX: Int, radiusZ: Int) {
		for(offsetX in -radiusX..radiusX) {
			for(offsetZ in -radiusZ..radiusZ) {
				world.getChunk(chunkX + offsetX, chunkZ + offsetZ) // UPDATE shitty hack to force nearby structures to gen first
			}
		}
	}
	
	abstract class OverworldFeature : Feature<NoFeatureConfig>(NoFeatureConfig.CODEC) {
		val feature
			get() = ConfiguredFeature(this, NO_FEATURE_CONFIG)
		
		final override fun generate(world: ISeedReader, generator: ChunkGenerator, rand: Random, pos: BlockPos, config: NoFeatureConfig): Boolean {
			return world.dimensionType == DimensionType.OVERWORLD && place(world, rand, pos, pos.x shr 4, pos.z shr 4)
		}
		
		protected abstract fun place(world: ISeedReader, rand: Random, pos: BlockPos, chunkX: Int, chunkZ: Int): Boolean
	}
	
	abstract class GeneratorTriggerBase : ITriggerHandler {
		final override fun check(world: World): Boolean {
			return !world.isRemote
		}
		
		final override fun update(entity: EntityTechnicalTrigger) {
			val world = entity.world as ServerWorld
			val pos = Pos(entity)
			val xz = pos.xz
			
			// TODO wait until either nearby chunks are loaded or a player gets close enough
			
			place(world, SharedSeedRandom().apply { setLargeFeatureSeed(world.seed, xz.chunkX, xz.chunkZ) }, pos)
			entity.remove()
		}
		
		final override fun nextTimer(rand: Random): Int {
			return 10
		}
		
		protected abstract fun place(world: ServerWorld, rand: Random, pos: BlockPos)
	}
}
