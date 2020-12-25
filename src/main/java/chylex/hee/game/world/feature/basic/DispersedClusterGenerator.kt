package chylex.hee.game.world.feature.basic

import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IClusterGenerator
import chylex.hee.game.world.component1
import chylex.hee.game.world.component2
import chylex.hee.game.world.feature.OverworldFeatures
import chylex.hee.game.world.feature.OverworldFeatures.OverworldFeature
import chylex.hee.game.world.getTile
import chylex.hee.game.world.isAir
import chylex.hee.game.world.setBlock
import chylex.hee.init.ModBlocks
import chylex.hee.system.random.nextInt
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.IWorld
import java.util.Random

object DispersedClusterGenerator : OverworldFeature() {
	private const val GRID_CHUNKS = 12
	
	private fun findSpawnAt(seed: Long, chunkX: Int, chunkZ: Int): ChunkPos? {
		val (startChunkX, startChunkZ) = OverworldFeatures.findStartChunkInGrid(GRID_CHUNKS, chunkX, chunkZ)
		val rand = Random((startChunkX * 483672793L) + (startChunkZ * 1054848967L) + seed)
		
		if (rand.nextInt(10) < 7) {
			return null
		}
		
		return ChunkPos(
			startChunkX + rand.nextInt(GRID_CHUNKS),
			startChunkZ + rand.nextInt(GRID_CHUNKS)
		)
	}
	
	override fun place(world: IWorld, rand: Random, pos: BlockPos, chunkX: Int, chunkZ: Int): Boolean {
		val (targetChunkX, targetChunkZ) = findSpawnAt(world.seed, chunkX, chunkZ) ?: return false
		
		if (chunkX != targetChunkX || chunkZ != targetChunkZ) {
			return false
		}
		
		for(spawnAttempt in 1..64) {
			val spawnPos = pos.add(
				rand.nextInt(16),
				rand.nextInt(4, 255 - (spawnAttempt * 3)),
				rand.nextInt(16)
			)
			
			if (!spawnPos.isAir(world)) {
				continue
			}
			
			for(searchAttempt in 1..4) {
				if (!spawnPos.add(rand.nextInt(-3, 3), rand.nextInt(-3, 3), rand.nextInt(-3, 3)).isAir(world)) {
					spawnPos.setBlock(world, ModBlocks.ENERGY_CLUSTER)
					spawnPos.getTile<TileEntityEnergyCluster>(world)?.loadClusterSnapshot(IClusterGenerator.OVERWORLD.generate(rand), inactive = true)
					return true
				}
			}
		}
		
		return false
	}
}
