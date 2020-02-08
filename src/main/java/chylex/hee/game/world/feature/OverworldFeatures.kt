package chylex.hee.game.world.feature
import chylex.hee.HEE
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.world.feature.basic.DispersedClusterGenerator
import chylex.hee.game.world.feature.energyshrine.EnergyShrineGenerator
import chylex.hee.game.world.feature.stronghold.StrongholdGenerator
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.Pos
import chylex.hee.system.util.named
import chylex.hee.system.util.xz
import net.minecraft.util.SharedSeedRandom
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
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.registries.ForgeRegistries
import java.util.Random
import java.util.function.Function

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object OverworldFeatures{
	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun onRegister(e: RegistryEvent.Register<Feature<*>>){
		with(e.registry){
			register(DispersedClusterGenerator named "dispersed_clusters")
			register(EnergyShrineGenerator named "energy_shrine")
			register(StrongholdGenerator named "stronghold")
		}
		
		for(biome in ForgeRegistries.BIOMES){
			biome.addFeature(TOP_LAYER_MODIFICATION, DispersedClusterGenerator.feature)
			biome.addFeature(TOP_LAYER_MODIFICATION, EnergyShrineGenerator.feature)
			biome.addFeature(TOP_LAYER_MODIFICATION, StrongholdGenerator.feature)
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
		val feature
			get() = ConfiguredFeature(this, NO_FEATURE_CONFIG)
		
		final override fun place(world: IWorld, generator: ChunkGenerator<out GenerationSettings>, rand: Random, pos: BlockPos, config: NoFeatureConfig): Boolean{
			return world.dimension.type == DimensionType.OVERWORLD && place(world, rand, pos, pos.x shr 4, pos.z shr 4)
		}
		
		protected abstract fun place(world: IWorld, rand: Random, pos: BlockPos, chunkX: Int, chunkZ: Int): Boolean
	}
	
	abstract class GeneratorTriggerBase : ITriggerHandler{
		final override fun check(world: World): Boolean{
			return !world.isRemote
		}
		
		final override fun update(entity: EntityTechnicalTrigger){
			val world = entity.world as ServerWorld
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
