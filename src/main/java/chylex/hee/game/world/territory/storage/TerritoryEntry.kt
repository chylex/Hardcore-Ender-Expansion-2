package chylex.hee.game.world.territory.storage
import chylex.hee.game.world.generation.TerritoryGenerationInfo
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.system.util.center
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.setPos
import chylex.hee.system.util.toYaw
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.INBTSerializable

class TerritoryEntry(private val owner: TerritoryGlobalStorage, private val instance: TerritoryInstance) : INBTSerializable<NBTTagCompound>{
	var lastSpawnPoint: BlockPos? by NotifyOnChange(null, owner::markDirty)
		private set
	
	var interestPoint: BlockPos? by NotifyOnChange(null, owner::markDirty)
		private set
	
	val spawnYaw
		get() = interestPoint?.center?.let { ip -> lastSpawnPoint?.center?.let { sp -> ip.subtract(sp).toYaw() } }
	
	fun initializeSpawnPoint(info: TerritoryGenerationInfo){
		if (lastSpawnPoint == null){
			val startChunk = instance.topLeftChunk
			
			lastSpawnPoint = info.spawnPoint.let { startChunk.getBlock(it.x, it.y, it.z) }
			interestPoint = info.interestPoint?.let { startChunk.getBlock(it.x, it.y, it.z) }
		}
	}
	
	fun changeSpawnPoint(pos: BlockPos){
		lastSpawnPoint = pos
	}
	
	override fun serializeNBT() = NBTTagCompound().apply {
		lastSpawnPoint?.let {
			setPos("LastSpawn", it)
		}
		
		interestPoint?.let {
			setPos("InterestPoint", it)
		}
	}
	
	override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
		lastSpawnPoint = getPosOrNull("LastSpawn")
		interestPoint = getPosOrNull("InterestPoint")
	}
}
