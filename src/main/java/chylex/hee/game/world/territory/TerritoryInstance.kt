package chylex.hee.game.world.territory
import chylex.hee.game.world.territory.TerritoryType.Companion.CHUNK_MARGIN
import chylex.hee.game.world.territory.TerritoryType.Companion.CHUNK_X_OFFSET
import chylex.hee.game.world.territory.TerritoryType.THE_HUB
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.floorToInt
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import java.util.Random
import kotlin.math.abs

// =======================================================
// DO NOT EVER CHANGE ANY CALCULATIONS IN THIS FILE. EVER.
// =======================================================

/*
 * Territories are generated in this manner:
 *
 *  7|5
 *  3|1
 *  -+-
 *  2|0
 *  6|4
 */

data class TerritoryInstance(val territory: TerritoryType, val index: Int){
	companion object{
		private const val HASH_ORDINAL_BITS = 8
		private const val HASH_ORDINAL_MASK = (1 shl HASH_ORDINAL_BITS) - 1
		
		val THE_HUB_INSTANCE = TerritoryInstance(THE_HUB, 0)
		
		fun fromHash(hash: Int): TerritoryInstance?{
			val territory = hash and HASH_ORDINAL_MASK
			val index = hash shr HASH_ORDINAL_BITS
			
			return TerritoryType.ALL.getOrNull(territory)?.let { TerritoryInstance(it, index) }
		}
		
		fun fromPos(pos: BlockPos): TerritoryInstance?{
			return fromPos(pos.x, pos.z)
		}
		
		fun fromPos(entity: Entity): TerritoryInstance?{
			return fromPos(entity.posX.floorToInt(), entity.posZ.floorToInt())
		}
		
		fun fromPos(x: Int, z: Int): TerritoryInstance?{
			val territory = TerritoryType.fromX(x) ?: return null
			
			if (territory.isSpawn){
				return THE_HUB_INSTANCE
			}
			
			val isPositiveZ = z >= -(CHUNK_MARGIN * 8)
			val adjustedZ = if (isPositiveZ) z else z + 1
			
			val blocksPerCell = (territory.chunks + CHUNK_MARGIN) * 16
			val cellIndex = (adjustedZ + (CHUNK_MARGIN * 8)) / blocksPerCell
			
			val indexStart = abs(cellIndex) * 4
			val indexOffsetX = if (x >= 0) 0 else 2
			val indexOffsetZ = if (isPositiveZ) 0 else 1
			
			return TerritoryInstance(territory, indexStart + indexOffsetX + indexOffsetZ)
		}
	}
	
	private val ordinal
		get() = territory.ordinal
	
	private val chunks
		get() = territory.chunks
	
	val hash
		get() = (ordinal and HASH_ORDINAL_MASK) or (index shl HASH_ORDINAL_BITS)
	
	val topLeftChunk: ChunkPos
		get(){
			val chunkOffX = if ((index / 2) % 2 == 0)
				(0 until ordinal).sumBy { CHUNK_MARGIN + TerritoryType.ALL[it].chunks } // positive x
			else
				-(1..ordinal).sumBy { CHUNK_MARGIN + TerritoryType.ALL[it].chunks } // negative x
			
			val distZ = if (index % 2 == 0)
				index / 4
			else
				-(index / 4) - 1 // moves odd indexes to negative z
			
			val chunkOffZ = distZ * (CHUNK_MARGIN + chunks)
			
			return ChunkPos(chunkOffX + CHUNK_X_OFFSET, chunkOffZ)
		}
	
	val bottomRightChunk: ChunkPos
		get() = topLeftChunk.let { ChunkPos(it.x + chunks - 1, it.z + chunks - 1) }
	
	val bottomCenterPos: BlockPos
		get() = topLeftChunk.getBlock(chunks * 8, territory.height.start, chunks * 8)
	
	fun generatesChunk(chunkX: Int, chunkZ: Int): Boolean{
		val (startX, startZ) = topLeftChunk
		return chunkX >= startX && chunkZ >= startZ && chunkX < startX + chunks && chunkZ < startZ + chunks
	}
	
	fun createRandom(world: World) = Random(world.seed).apply {
		setSeed(nextLong() xor (66L * index) + (ordinal * nextInt()))
	}
}
