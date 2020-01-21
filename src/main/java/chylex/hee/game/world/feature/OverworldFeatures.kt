package chylex.hee.game.world.feature
import chylex.hee.HEE
import chylex.hee.game.world.feature.basic.DispersedClusterGenerator
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.named
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.ChunkGenerator
import net.minecraft.world.gen.GenerationSettings
import net.minecraft.world.gen.GenerationStage.Decoration.TOP_LAYER_MODIFICATION
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.IFeatureConfig.NO_FEATURE_CONFIG
import net.minecraft.world.gen.feature.NoFeatureConfig
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.registries.ForgeRegistries
import java.util.Random
import java.util.function.Function

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object OverworldFeatures{
	fun register(){
		// UPDATE GameRegistry.registerWorldGenerator(EnergyShrineGenerator, Int.MAX_VALUE - 1)
		// UPDATE GameRegistry.registerWorldGenerator(StrongholdGenerator, Int.MAX_VALUE - 2)
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun onRegister(e: RegistryEvent.Register<Feature<*>>){
		with(e.registry){
			register(DispersedClusterGenerator named "dispersed_clusters")
		}
		
		for(biome in ForgeRegistries.BIOMES){
			biome.addFeature(TOP_LAYER_MODIFICATION, ConfiguredFeature(DispersedClusterGenerator, NO_FEATURE_CONFIG))
		}
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
	
	abstract class OverworldFeature : Feature<NoFeatureConfig>(Function { NoFeatureConfig.deserialize(it) }, false){
		final override fun place(world: IWorld, generator: ChunkGenerator<out GenerationSettings>, rand: Random, pos: BlockPos, config: NoFeatureConfig): Boolean{
			return world.dimension.type == DimensionType.OVERWORLD && place(world, rand, pos, pos.x shr 4, pos.z shr 4)
		}
		
		protected abstract fun place(world: IWorld, rand: Random, pos: BlockPos, chunkX: Int, chunkZ: Int): Boolean
	}
}
