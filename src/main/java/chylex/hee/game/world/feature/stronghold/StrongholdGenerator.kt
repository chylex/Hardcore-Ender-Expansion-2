package chylex.hee.game.world.feature.stronghold

import chylex.hee.HEE
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.STRONGHOLD_GENERATOR
import chylex.hee.game.world.Pos
import chylex.hee.game.world.component1
import chylex.hee.game.world.component2
import chylex.hee.game.world.feature.OverworldFeatures
import chylex.hee.game.world.feature.OverworldFeatures.GeneratorTriggerBase
import chylex.hee.game.world.feature.OverworldFeatures.OverworldFeature
import chylex.hee.game.world.feature.OverworldFeatures.preloadChunks
import chylex.hee.game.world.generation.WorldToStructureWorldAdapter
import chylex.hee.game.world.math.PosXZ
import chylex.hee.game.world.perDimensionData
import chylex.hee.game.world.structure.piece.IStructureBuild
import chylex.hee.system.random.nextInt
import chylex.hee.system.serialization.NBTList.Companion.putList
import chylex.hee.system.serialization.NBTObjectList
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getListOfCompounds
import chylex.hee.system.serialization.getPos
import chylex.hee.system.serialization.putPos
import chylex.hee.system.serialization.use
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.IWorld
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.WorldSavedData
import java.util.Random
import kotlin.math.abs

object StrongholdGenerator : OverworldFeature() {
	private const val GRID_CHUNKS = 68
	private const val DIST_CHUNKS = 18
	
	private val STRUCTURE_SIZE
		get() = StrongholdPieces.STRUCTURE_SIZE
	
	class DimensionStrongholdData private constructor() : WorldSavedData(NAME) {
		companion object {
			fun get(world: ServerWorld) = world.perDimensionData(NAME, ::DimensionStrongholdData)
			
			private const val NAME = "HEE_STRONGHOLDS"
			
			private const val CHUNK_X_TAG = "ChunkX"
			private const val CHUNK_Z_TAG = "ChunkZ"
			private const val POS_TAG = "Pos"
			private const val LOCATIONS_TAG = "Locations"
		}
		
		private val locations = mutableMapOf<ChunkPos, BlockPos>()
		
		fun addLocation(chunk: ChunkPos, pos: BlockPos) {
			locations[chunk] = pos
			markDirty()
		}
		
		fun getLocation(chunk: ChunkPos): BlockPos? {
			return locations[chunk]
		}
		
		override fun write(nbt: TagCompound) = nbt.apply {
			val list = NBTObjectList<TagCompound>()
			
			for((chunk, pos) in locations) {
				list.append(TagCompound().also {
					it.putInt(CHUNK_X_TAG, chunk.x)
					it.putInt(CHUNK_Z_TAG, chunk.z)
					it.putPos(POS_TAG, pos)
				})
			}
			
			putList(LOCATIONS_TAG, list)
		}
		
		override fun read(nbt: TagCompound) = nbt.use {
			locations.clear()
			
			for(tag in getListOfCompounds(LOCATIONS_TAG)) {
				val chunk = ChunkPos(tag.getInt(CHUNK_X_TAG), tag.getInt(CHUNK_Z_TAG))
				locations[chunk] = tag.getPos(POS_TAG)
			}
		}
	}
	
	// Search
	
	private fun findSpawnAt(seed: Long, chunkX: Int, chunkZ: Int): BlockPos? {
		val (startChunkX, startChunkZ) = OverworldFeatures.findStartChunkInGrid(GRID_CHUNKS, chunkX, chunkZ)
		
		val centerChunkX = startChunkX + (GRID_CHUNKS / 2)
		val centerChunkZ = startChunkZ + (GRID_CHUNKS / 2)
		
		val rand = Random((centerChunkX * 920419813L) + (centerChunkZ * 49979687L) + seed)
		
		if (rand.nextInt(5) > 2 || abs(centerChunkX) < 50 || abs(centerChunkZ) < 50) {
			return null
		}
		
		return Pos(
			(centerChunkX + rand.nextInt(-DIST_CHUNKS, DIST_CHUNKS)) * 16 + 8,
			(STRUCTURE_SIZE.centerY + rand.nextInt(4, 14)),
			(centerChunkZ + rand.nextInt(-DIST_CHUNKS, DIST_CHUNKS)) * 16 + 8
		)
	}
	
	private fun isChunkPopulated(world: ServerWorld, chunk: ChunkPos): Boolean {
		return false // UPDATE (world.chunkProvider as? ServerChunkProvider)?.loadChunk(chunk.x, chunk.z).let { it != null && it.isTerrainPopulated }
	}
	
	fun findNearest(world: ServerWorld, xz: PosXZ): BlockPos? {
		val chunkX = xz.chunkX
		val chunkZ = xz.chunkZ
		
		val seed = world.seed
		
		val strongholds = DimensionStrongholdData.get(world)
		val found = mutableListOf<BlockPos>()
		
		for(offX in -2..2) for(offZ in -2..2) {
			val testChunkX = chunkX + (GRID_CHUNKS * offX)
			val testChunkZ = chunkZ + (GRID_CHUNKS * offZ)
			val foundPos = findSpawnAt(seed, testChunkX, testChunkZ)
			
			if (foundPos == null) {
				continue
			}
			
			val foundChunk = ChunkPos(foundPos)
			val realPos = strongholds.getLocation(foundChunk)
			
			if (realPos != null) {
				found.add(realPos)
			}
			else if (!isChunkPopulated(world, foundChunk)) {
				found.add(foundPos)
			}
		}
		
		return found.minByOrNull { PosXZ(it).distanceSqTo(xz) }
	}
	
	// Helpers
	
	private fun buildStructure(rand: Random): Pair<IStructureBuild?, BlockPos?> {
		for(attempt in 1..50) {
			return StrongholdBuilder.buildWithEyeOfEnderTarget(rand) ?: continue
		}
		
		return Pair(null, null)
	}
	
	// Generation
	
	override fun place(world: IWorld, rand: Random, pos: BlockPos, chunkX: Int, chunkZ: Int): Boolean {
		val spawnPos = findSpawnAt(world.seed, chunkX, chunkZ) ?: return false
		
		if (ChunkPos(spawnPos).let { it.x != chunkX || it.z != chunkZ }) {
			return false
		}
		
		EntityTechnicalTrigger(world.world, STRONGHOLD_GENERATOR).apply {
			setLocationAndAngles(spawnPos.x + 0.5, spawnPos.y + 0.5, spawnPos.z + 0.5, 0F, 0F)
			world.addEntity(this)
		}
		
		return true
	}
	
	object GeneratorTrigger : GeneratorTriggerBase() {
		override fun place(world: ServerWorld, rand: Random, pos: BlockPos) {
			val chunk = ChunkPos(pos)
			val (build, targetPos) = buildStructure(rand)
			
			if (build == null) {
				HEE.log.error("[StrongholdGenerator] failed all attempts at generating (chunkX = ${chunk.x}, chunkZ = ${chunk.z}, seed = ${world.seed})")
				return
			}
			
			val offset = pos.subtract(STRUCTURE_SIZE.centerPos)
			
			preloadChunks(world, chunk.x, chunk.z, STRUCTURE_SIZE.centerX / 16, STRUCTURE_SIZE.centerZ / 16)
			WorldToStructureWorldAdapter(world, rand, offset).apply(build::generate).finalize()
			DimensionStrongholdData.get(world).addLocation(chunk, targetPos?.add(pos) ?: pos)
		}
	}
}
