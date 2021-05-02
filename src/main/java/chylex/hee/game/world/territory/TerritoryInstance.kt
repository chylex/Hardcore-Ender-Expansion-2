package chylex.hee.game.world.territory

import chylex.hee.HEE
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.mechanics.portal.SpawnInfo
import chylex.hee.game.world.center
import chylex.hee.game.world.component1
import chylex.hee.game.world.component2
import chylex.hee.game.world.territory.TerritoryType.Companion.CHUNK_MARGIN
import chylex.hee.game.world.territory.TerritoryType.Companion.CHUNK_X_OFFSET
import chylex.hee.game.world.territory.TerritoryType.THE_HUB
import chylex.hee.game.world.territory.storage.TerritoryGlobalStorage
import chylex.hee.game.world.territory.storage.TerritoryStorageComponent
import chylex.hee.proxy.Environment
import chylex.hee.system.math.floorToInt
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntityTameable
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING
import net.minecraft.world.server.ServerWorld
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

data class TerritoryInstance(val territory: TerritoryType, val index: Int) {
	companion object {
		private const val HASH_ORDINAL_BITS = 8
		private const val HASH_ORDINAL_MASK = (1 shl HASH_ORDINAL_BITS) - 1
		
		val THE_HUB_INSTANCE = TerritoryInstance(THE_HUB, 0)
		
		fun fromHash(hash: Int): TerritoryInstance? {
			val territory = hash and HASH_ORDINAL_MASK
			val index = hash shr HASH_ORDINAL_BITS
			
			return TerritoryType.ALL.getOrNull(territory)?.let { TerritoryInstance(it, index) }
		}
		
		fun fromPos(pos: BlockPos): TerritoryInstance? {
			return fromPos(pos.x, pos.z)
		}
		
		fun fromPos(entity: Entity): TerritoryInstance? {
			return fromPos(entity.posX.floorToInt(), entity.posZ.floorToInt())
		}
		
		fun fromPos(x: Int, z: Int): TerritoryInstance? {
			val territory = TerritoryType.fromX(x) ?: return null
			
			if (territory.isSpawn) {
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
		
		val endWorld: ServerWorld
			get() = Environment.getServer().getWorld(HEE.dim)
	}
	
	private val ordinal
		get() = territory.ordinal
	
	private val chunks
		get() = territory.chunks
	
	val hash
		get() = (ordinal and HASH_ORDINAL_MASK) or (index shl HASH_ORDINAL_BITS)
	
	val topLeftChunk by lazy(LazyThreadSafetyMode.PUBLICATION) {
		val chunkOffX = if ((index / 2) % 2 == 0)
			(0 until ordinal).sumBy { CHUNK_MARGIN + TerritoryType.ALL[it].chunks } // positive x
		else
			-(1..ordinal).sumBy { CHUNK_MARGIN + TerritoryType.ALL[it].chunks } // negative x
		
		val distZ = if (index % 2 == 0)
			index / 4
		else
			-(index / 4) - 1 // moves odd indexes to negative z
		
		val chunkOffZ = distZ * (CHUNK_MARGIN + chunks)
		
		ChunkPos(chunkOffX + CHUNK_X_OFFSET, chunkOffZ)
	}
	
	private val bottomCenterPos: BlockPos
		get() = topLeftChunk.getBlock(chunks * 8, territory.height.first, chunks * 8)
	
	private val fallbackSpawnPoint: BlockPos
		get() = bottomCenterPos.let { endWorld.getHeight(MOTION_BLOCKING, it) }.up()
	
	val centerPoint
		get() = topLeftChunk.getBlock(chunks * 8, territory.height.let { (it.first + it.last) / 2 }, chunks * 8).center
	
	val players
		get() = endWorld.players.filter { this == fromPos(it) }
	
	val storage
		get() = TerritoryGlobalStorage.get().forInstance(this)
	
	fun generatesChunk(chunkX: Int, chunkZ: Int): Boolean {
		val (startX, startZ) = topLeftChunk
		return chunkX >= startX && chunkZ >= startZ && chunkX < startX + chunks && chunkZ < startZ + chunks
	}
	
	fun createRandom(worldSeed: Long) = Random(worldSeed).apply {
		setSeed(nextLong() xor (66L * index) + (ordinal * nextInt()))
	}
	
	inline fun <reified T : TerritoryStorageComponent> getStorageComponent(): T? {
		return storage?.getComponent(T::class.java)
	}
	
	fun getSpawnPoint(): BlockPos {
		return storage?.loadSpawn() ?: fallbackSpawnPoint
	}
	
	fun getSpawnPoint(player: EntityPlayer): BlockPos {
		return storage?.loadSpawnForPlayer(player) ?: fallbackSpawnPoint
	}
	
	fun updateSpawnPoint(player: EntityPlayer, pos: BlockPos) {
		storage?.updateSpawnForPlayer(player, pos)
	}
	
	fun prepareSpawnPoint(entity: Entity?, clearanceRadius: Int): SpawnInfo {
		val world = endWorld
		val spawnPoint = entity?.let(::determineOwningPlayer)?.let(::getSpawnPoint) ?: getSpawnPoint()
		
		BlockAbstractPortal.ensureClearance(world, spawnPoint, clearanceRadius)
		BlockAbstractPortal.ensurePlatform(world, spawnPoint, territory.gen.groundBlock, clearanceRadius)
		
		territory.desc.prepareSpawnPoint(world, spawnPoint, this)
		return SpawnInfo(spawnPoint, storage?.spawnYaw)
	}
	
	@Suppress("UNNECESSARY_SAFE_CALL")
	private fun determineOwningPlayer(entity: Entity) = when(entity) {
		is EntityPlayer   -> entity
		is EntityItem     -> entity.throwerId?.let(Environment.getServer().playerList::getPlayerByUUID)
		is EntityTameable -> entity.owner as? EntityPlayer
		else              -> null
	}
}
