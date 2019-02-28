package chylex.hee.game.world.feature.basic
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IClusterGenerator
import chylex.hee.game.world.feature.OverworldFeatures
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.getTile
import chylex.hee.system.util.isAir
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.setBlock
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.DimensionType.OVERWORLD
import net.minecraft.world.World
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.IChunkGenerator
import net.minecraftforge.fml.common.IWorldGenerator
import java.util.Random

object DispersedClusterGenerator : IWorldGenerator{
	private const val GRID_CHUNKS = 12
	
	private fun findSpawnAt(seed: Long, chunkX: Int, chunkZ: Int): ChunkPos?{
		val (startChunkX, startChunkZ) = OverworldFeatures.findStartChunkInGrid(GRID_CHUNKS, chunkX, chunkZ)
		val rand = Random((startChunkX * 483672793L) + (startChunkZ * 1054848967L) + seed)
		
		if (rand.nextInt(10) < 7){
			return null
		}
		
		return ChunkPos(
			startChunkX + rand.nextInt(GRID_CHUNKS),
			startChunkZ + rand.nextInt(GRID_CHUNKS)
		)
	}
	
	override fun generate(rand: Random, chunkX: Int, chunkZ: Int, world: World, generator: IChunkGenerator, provider: IChunkProvider){
		if (world.provider.dimensionType != OVERWORLD){
			return
		}
		
		val (targetChunkX, targetChunkZ) = findSpawnAt(world.seed, chunkX, chunkZ) ?: return
		
		if (chunkX != targetChunkX || chunkZ != targetChunkZ){
			return
		}
		
		val blockX = (chunkX * 16) + 8
		val blockZ = (chunkZ * 16) + 8
		
		for(spawnAttempt in 1..64){
			val spawnPos = Pos(
				blockX + rand.nextInt(16),
				rand.nextInt(4, 255 - (spawnAttempt * 3)),
				blockZ + rand.nextInt(16)
			)
			
			if (!spawnPos.isAir(world)){
				continue
			}
			
			for(searchAttempt in 1..4){
				if (!spawnPos.add(rand.nextInt(-3, 3), rand.nextInt(-3, 3), rand.nextInt(-3, 3)).isAir(world)){
					spawnPos.setBlock(world, ModBlocks.ENERGY_CLUSTER)
					spawnPos.getTile<TileEntityEnergyCluster>(world)?.loadClusterSnapshot(IClusterGenerator.OVERWORLD.generate(rand))
					return
				}
			}
		}
	}
}
