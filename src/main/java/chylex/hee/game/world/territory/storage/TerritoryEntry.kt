package chylex.hee.game.world.territory.storage
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getPos
import chylex.hee.system.util.setPos
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.INBTSerializable

class TerritoryEntry(private val owner: TerritoryGlobalStorage, private val instance: TerritoryInstance) : INBTSerializable<NBTTagCompound>{
	var lastSpawnPos: BlockPos by NotifyOnChange(instance.bottomCenterPos.up(2), owner::markDirty) // TODO
	
	override fun serializeNBT() = NBTTagCompound().apply {
		setPos("LastSpawn", lastSpawnPos)
	}
	
	override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
		lastSpawnPos = getPos("LastSpawn")
	}
}
