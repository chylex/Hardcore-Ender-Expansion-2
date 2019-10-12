package chylex.hee.game.world.feature.stronghold
import chylex.hee.HEE
import chylex.hee.game.world.feature.OverworldFeatures
import chylex.hee.game.world.feature.OverworldFeatures.preloadChunks
import chylex.hee.game.world.structure.piece.IStructureBuild
import chylex.hee.game.world.structure.world.WorldToStructureWorldAdapter
import chylex.hee.game.world.util.PosXZ
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.NBTObjectList
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.getPos
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.perDimensionData
import chylex.hee.system.util.setPos
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.DimensionType.OVERWORLD
import net.minecraft.world.World
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.ChunkProviderServer
import net.minecraft.world.gen.IChunkGenerator
import net.minecraft.world.storage.WorldSavedData
import net.minecraftforge.fml.common.IWorldGenerator
import java.util.Random
import kotlin.math.abs

object StrongholdGenerator : IWorldGenerator{
	private const val GRID_CHUNKS = 68
	private const val DIST_CHUNKS = 18
	
	private val STRUCTURE_SIZE
		get() = StrongholdPieces.STRUCTURE_SIZE
	
	class DimensionStrongholdData(name: String) : WorldSavedData(name){ // must be public for reflection
		companion object{
			fun get(world: World) = world.perDimensionData("HEE_STRONGHOLDS", ::DimensionStrongholdData)
			
			private const val CHUNK_X_TAG = "ChunkX"
			private const val CHUNK_Z_TAG = "ChunkZ"
			private const val POS_TAG = "Pos"
			private const val LOCATIONS_TAG = "Locations"
		}
		
		private val locations = mutableMapOf<ChunkPos, BlockPos>()
		
		fun addLocation(chunk: ChunkPos, pos: BlockPos){
			locations[chunk] = pos
			markDirty()
		}
		
		fun getLocation(chunk: ChunkPos): BlockPos?{
			return locations[chunk]
		}
		
		override fun writeToNBT(nbt: TagCompound) = nbt.apply {
			val list = NBTObjectList<TagCompound>()
			
			for((chunk, pos) in locations){
				list.append(TagCompound().also {
					it.setInteger(CHUNK_X_TAG, chunk.x)
					it.setInteger(CHUNK_Z_TAG, chunk.z)
					it.setPos(POS_TAG, pos)
				})
			}
			
			setList(LOCATIONS_TAG, list)
		}
		
		override fun readFromNBT(nbt: TagCompound) = with(nbt){
			locations.clear()
			
			for(tag in getListOfCompounds(LOCATIONS_TAG)){
				val chunk = ChunkPos(tag.getInteger(CHUNK_X_TAG), tag.getInteger(CHUNK_Z_TAG))
				locations[chunk] = tag.getPos(POS_TAG)
			}
		}
	}
	
	// Search
	
	private fun findSpawnAt(seed: Long, chunkX: Int, chunkZ: Int): BlockPos?{
		val (startChunkX, startChunkZ) = OverworldFeatures.findStartChunkInGrid(GRID_CHUNKS, chunkX, chunkZ)
		
		val centerChunkX = startChunkX + (GRID_CHUNKS / 2)
		val centerChunkZ = startChunkZ + (GRID_CHUNKS / 2)
		
		val rand = Random((centerChunkX * 920419813L) + (centerChunkZ * 49979687L) + seed)
		
		if (rand.nextInt(5) > 2 || abs(centerChunkX) < 50 || abs(centerChunkZ) < 50){
			return null
		}
		
		return Pos(
			(centerChunkX + rand.nextInt(-DIST_CHUNKS, DIST_CHUNKS)) * 16 + 8,
			(STRUCTURE_SIZE.centerY + rand.nextInt(4, 14)),
			(centerChunkZ + rand.nextInt(-DIST_CHUNKS, DIST_CHUNKS)) * 16 + 8
		)
	}
	
	private fun isChunkPopulated(world: World, chunk: ChunkPos): Boolean{
		return (world.chunkProvider as? ChunkProviderServer)?.loadChunk(chunk.x, chunk.z).let { it != null && it.isTerrainPopulated }
	}
	
	fun findNearest(world: World, xz: PosXZ): BlockPos?{
		val chunkX = xz.chunkX
		val chunkZ = xz.chunkZ
		
		val seed = world.seed
		
		val strongholds = DimensionStrongholdData.get(world)
		val found = mutableListOf<BlockPos>()
		
		for(offX in -2..2) for(offZ in -2..2){
			val testChunkX = chunkX + (GRID_CHUNKS * offX)
			val testChunkZ = chunkZ + (GRID_CHUNKS * offZ)
			val foundPos = findSpawnAt(seed, testChunkX, testChunkZ)
			
			if (foundPos == null){
				continue
			}
			
			val foundChunk = ChunkPos(foundPos)
			val realPos = strongholds.getLocation(foundChunk)
			
			if (realPos != null){
				found.add(realPos)
			}
			else if (!isChunkPopulated(world, foundChunk)){
				found.add(foundPos)
			}
		}
		
		return found.minBy { PosXZ(it).distanceSqTo(xz) }
	}
	
	// Helpers
	
	private fun buildStructure(rand: Random): Pair<IStructureBuild?, BlockPos?>{
		for(attempt in 1..50){
			return StrongholdBuilder.buildWithEyeOfEnderTarget(rand) ?: continue
		}
		
		return Pair(null, null)
	}
	
	// Generation
	
	override fun generate(rand: Random, chunkX: Int, chunkZ: Int, world: World, generator: IChunkGenerator, provider: IChunkProvider){
		if (world.provider.dimensionType != OVERWORLD){
			return
		}
		
		val centerPos = findSpawnAt(world.seed, chunkX, chunkZ) ?: return
		
		if (chunkX != (centerPos.x shr 4) || chunkZ != (centerPos.z shr 4)){
			return
		}
		
		val (build, targetPos) = buildStructure(rand)
		
		if (build == null){
			HEE.log.error("[StrongholdGenerator] failed all attempts at generating (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
			return
		}
		
		val offset = centerPos.subtract(STRUCTURE_SIZE.centerPos)
		val chunk = ChunkPos(chunkX, chunkZ)
		
		preloadChunks(world, chunkX, chunkZ, STRUCTURE_SIZE.centerX / 16, STRUCTURE_SIZE.centerZ / 16)
		WorldToStructureWorldAdapter(world, rand, offset).apply(build::generate).finalize()
		DimensionStrongholdData.get(world).addLocation(chunk, targetPos?.add(centerPos) ?: centerPos)
	}
}
