package chylex.hee.game.world.territory.storage
import chylex.hee.game.world.ChunkGeneratorEndCustom
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.system.util.Pos
import chylex.hee.system.util.center
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getLongArrayOrNull
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.setLongArray
import chylex.hee.system.util.setPos
import chylex.hee.system.util.toYaw
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.gen.ChunkProviderServer
import net.minecraftforge.common.util.INBTSerializable
import java.util.UUID

class TerritoryEntry(private val owner: TerritoryGlobalStorage, private val instance: TerritoryInstance) : INBTSerializable<NBTTagCompound>{
	private var spawnPoint: BlockPos? by NotifyOnChange(null, owner::markDirty)
	private var interestPoint: BlockPos? by NotifyOnChange(null, owner::markDirty)
	
	val spawnYaw
		get() = interestPoint?.center?.let { ip -> spawnPoint?.center?.let { sp -> ip.subtract(sp).toYaw() } }
	
	private val lastPortals = mutableMapOf<UUID, BlockPos>()
	
	fun loadSpawn(world: World): BlockPos{
		if (spawnPoint == null){
			val info = ((world.chunkProvider as ChunkProviderServer).chunkGenerator as ChunkGeneratorEndCustom).getGenerationInfo(instance)
			
			val startChunk = instance.topLeftChunk
			val bottomY = instance.territory.height.first
			
			spawnPoint = info.spawnPoint.let { startChunk.getBlock(it.x, bottomY + it.y, it.z) }
			interestPoint = info.interestPoint?.let { startChunk.getBlock(it.x, bottomY + it.y, it.z) }
		}
		
		return spawnPoint!!
	}
	
	fun loadSpawnForPlayer(world: World, player: EntityPlayer): BlockPos{
		return lastPortals[player.uniqueID] ?: loadSpawn(world)
	}
	
	fun updateSpawnForPlayer(player: EntityPlayer, pos: BlockPos){
		if (pos == spawnPoint){
			if (lastPortals.remove(player.uniqueID) != null){
				owner.markDirty()
			}
		}
		else if (lastPortals.put(player.uniqueID, pos) != pos){
			owner.markDirty()
		}
	}
	
	override fun serializeNBT() = NBTTagCompound().apply {
		spawnPoint?.let {
			setPos("Spawn", it)
		}
		
		interestPoint?.let {
			setPos("Interest", it)
		}
		
		if (lastPortals.isNotEmpty()){
			val lastPortalArray = LongArray(lastPortals.size * 3)
			
			for((index, entry) in lastPortals.entries.withIndex()){
				val offset = index * 3
				val (uuid, pos) = entry
				
				lastPortalArray[offset + 0] = uuid.mostSignificantBits
				lastPortalArray[offset + 1] = uuid.leastSignificantBits
				lastPortalArray[offset + 2] = pos.toLong()
			}
			
			setLongArray("LastPortals", lastPortalArray)
		}
	}
	
	override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
		spawnPoint = getPosOrNull("Spawn")
		interestPoint = getPosOrNull("Interest")
		
		lastPortals.clear()
		
		val lastPortalArray = getLongArrayOrNull("LastPortals")
		
		if (lastPortalArray != null){
			for(index in 0 until lastPortalArray.size step 3){
				lastPortals[UUID(lastPortalArray[index + 0], lastPortalArray[index + 1])] = Pos(lastPortalArray[2])
			}
		}
	}
}
