package chylex.hee.game.world.feature.stronghold
import chylex.hee.HEE
import chylex.hee.game.world.feature.OverworldFeatures
import chylex.hee.game.world.structure.world.WorldToStructureWorldAdapter
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.NBTObjectList
import chylex.hee.system.util.Pos
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.getPos
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.perDimensionData
import chylex.hee.system.util.setPos
import net.minecraft.nbt.NBTTagCompound
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
	
	class DimensionStrongholdData(name: String) : WorldSavedData(name){ // must be public for reflection
		companion object{
			fun get(world: World) = world.perDimensionData("HEE_STRONGHOLDS", ::DimensionStrongholdData)
		}
		
		private val locations = mutableMapOf<ChunkPos, BlockPos>()
		
		fun addLocation(chunk: ChunkPos, pos: BlockPos){
			locations[chunk] = pos
			markDirty()
		}
		
		fun getLocation(chunk: ChunkPos): BlockPos?{
			return locations[chunk]
		}
		
		override fun writeToNBT(nbt: NBTTagCompound) = nbt.apply {
			val list = NBTObjectList<NBTTagCompound>()
			
			for((chunk, pos) in locations){
				list.append(NBTTagCompound().also {
					it.setInteger("ChunkX", chunk.x)
					it.setInteger("ChunkZ", chunk.z)
					it.setPos("Pos", pos)
				})
			}
			
			setList("Locations", list)
		}
		
		override fun readFromNBT(nbt: NBTTagCompound) = with(nbt){
			locations.clear()
			
			for(tag in getListOfCompounds("Locations")){
				val chunk = ChunkPos(tag.getInteger("ChunkX"), tag.getInteger("ChunkZ"))
				locations[chunk] = tag.getPos("Pos")
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
			(StrongholdPieces.STRUCTURE_SIZE.centerY + rand.nextInt(4, 14)),
			(centerChunkZ + rand.nextInt(-DIST_CHUNKS, DIST_CHUNKS)) * 16 + 8
		)
	}
	
	private fun isChunkPopulated(world: World, chunk: ChunkPos): Boolean{
		return (world.chunkProvider as? ChunkProviderServer)?.loadChunk(chunk.x, chunk.z).let { it != null && it.isTerrainPopulated }
	}
	
	fun findNearest(world: World, x: Double, z: Double): BlockPos?{
		val blockX = x.floorToInt()
		val blockZ = z.floorToInt()
		
		val chunkX = blockX shr 4
		val chunkZ = blockZ shr 4
		
		val seed = world.seed
		
		val strongholds = DimensionStrongholdData.get(world)
		val found = mutableListOf<BlockPos>()
		
		for(offX in -2..2){
			for(offZ in -2..2){
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
		}
		
		return found.minBy { it.distanceSqTo(blockX, it.y, blockZ) }
	}
	
	// Generation
	
	private fun preloadChunks(world: World, chunkX: Int, chunkZ: Int){
		val chunkRadius = StrongholdPieces.STRUCTURE_SIZE.centerX / 16
		
		for(offsetX in -chunkRadius..chunkRadius){
			for(offsetZ in -chunkRadius..chunkRadius){
				world.getChunk(chunkX + offsetX, chunkZ + offsetZ) // UPDATE shitty hack to force nearby structures to gen first
			}
		}
	}
	
	override fun generate(rand: Random, chunkX: Int, chunkZ: Int, world: World, generator: IChunkGenerator, provider: IChunkProvider){
		if (world.provider.dimensionType != OVERWORLD){
			return
		}
		
		val centerPos = findSpawnAt(world.seed, chunkX, chunkZ) ?: return
		
		if (chunkX != (centerPos.x shr 4) || chunkZ != (centerPos.z shr 4)){
			return
		}
		
		val adapter = WorldToStructureWorldAdapter(world, rand, centerPos.subtract(StrongholdPieces.STRUCTURE_SIZE.centerPos))
		
		for(attempt in 1..50){
			val (build, targetPos) = StrongholdBuilder.buildWithEyeOfEnderTarget(rand) ?: continue
			
			preloadChunks(world, chunkX, chunkZ)
			adapter.apply(build::generate).finalize()
			
			DimensionStrongholdData.get(world).addLocation(ChunkPos(chunkX, chunkZ), targetPos?.add(centerPos) ?: centerPos)
			return
		}
		
		HEE.log.error("[StrongholdGenerator] failed all attempts at generating (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
	}
}
