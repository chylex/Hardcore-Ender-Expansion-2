package chylex.hee.game.world

import chylex.hee.game.mechanics.portal.SpawnInfo
import chylex.hee.game.world.provider.DragonFightManagerNull
import chylex.hee.game.world.provider.WorldBorderNull
import chylex.hee.game.world.territory.TerritoryInstance.Companion.THE_HUB_INSTANCE
import chylex.hee.game.world.territory.TerritoryTicker
import chylex.hee.game.world.territory.TerritoryVoid
import net.minecraft.server.MinecraftServer
import net.minecraft.util.RegistryKey
import net.minecraft.util.math.BlockPos
import net.minecraft.world.DimensionType
import net.minecraft.world.GameRules.DO_WEATHER_CYCLE
import net.minecraft.world.World
import net.minecraft.world.biome.IBiomeMagnifier
import net.minecraft.world.chunk.listener.IChunkStatusListener
import net.minecraft.world.gen.ChunkGenerator
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.spawner.ISpecialSpawner
import net.minecraft.world.storage.IServerWorldInfo
import net.minecraft.world.storage.SaveFormat.LevelSave
import java.util.concurrent.Executor

class ServerWorldEndCustom(
	server: MinecraftServer,
	backgroundExecutor: Executor,
	levelSave: LevelSave,
	serverWorldInfo: IServerWorldInfo,
	dimension: RegistryKey<World>,
	dimensionType: DimensionType,
	statusListener: IChunkStatusListener,
	chunkGenerator: ChunkGenerator,
	isDebug: Boolean,
	seed: Long,
	specialSpawners: MutableList<ISpecialSpawner>,
	shouldBeTicking: Boolean,
) : ServerWorld(server, backgroundExecutor, levelSave, serverWorldInfo, dimension, patchDimensionType(dimensionType), statusListener, chunkGenerator, isDebug, seed, specialSpawners, shouldBeTicking) {
	private companion object {
		private fun patchDimensionType(dimensionType: DimensionType) = dimensionType.apply {
			magnifier = IBiomeMagnifier { _, x, y, z, biomeReader -> biomeReader.getNoiseBiome(x, y, z) }
		}
	}
	
	init {
		(chunkGenerator as ChunkGeneratorEndCustom).setSeed(seed)
		
		worldBorder = WorldBorderNull()
		dragonFightManager = object : DragonFightManagerNull(this) {
			override fun tick() {
				this@ServerWorldEndCustom.tickNonIdle()
			}
		}
	}
	
	/**
	 * Stops ticking a few seconds after all players leave the dimension.
	 */
	private fun tickNonIdle() {
		TerritoryTicker.onWorldTick(this)
		TerritoryVoid.onWorldTick(this)
	}
	
	/**
	 * Ticks all the time.
	 */
	override fun tickWorld() {
		super.tickWorld()
		
		gameRules[DO_WEATHER_CYCLE].let {
			if (it.get()) {
				it.set(false, server)
			}
		}
		
		prevRainingStrength = 0F
		rainingStrength = 0F
		prevThunderingStrength = 0F
		thunderingStrength = 0F
	}
	
	override fun getSpawnAngle(): Float {
		return getSpawnInfo().yaw ?: super.getSpawnAngle() // TODO
	}
	
	override fun getSpawnPoint(): BlockPos {
		return getSpawnInfo().pos
	}
	
	fun getSpawnInfo(): SpawnInfo {
		return THE_HUB_INSTANCE.prepareSpawnPoint(null, clearanceRadius = 1)
	}
}
