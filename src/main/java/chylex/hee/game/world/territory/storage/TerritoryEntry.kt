package chylex.hee.game.world.territory.storage
import chylex.hee.game.world.ChunkGeneratorEndCustom
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.system.util.center
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.setPos
import chylex.hee.system.util.toYaw
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.gen.ChunkProviderServer
import net.minecraftforge.common.util.INBTSerializable

class TerritoryEntry(private val owner: TerritoryGlobalStorage, private val instance: TerritoryInstance) : INBTSerializable<NBTTagCompound>{
	private var spawnPoint: BlockPos? by NotifyOnChange(null, owner::markDirty)
	private var interestPoint: BlockPos? by NotifyOnChange(null, owner::markDirty)
	
	val spawnYaw
		get() = interestPoint?.center?.let { ip -> spawnPoint?.center?.let { sp -> ip.subtract(sp).toYaw() } }
	
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
	
	override fun serializeNBT() = NBTTagCompound().apply {
		spawnPoint?.let {
			setPos("Spawn", it)
		}
		
		interestPoint?.let {
			setPos("Interest", it)
		}
	}
	
	override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
		spawnPoint = getPosOrNull("Spawn")
		interestPoint = getPosOrNull("Interest")
	}
}
