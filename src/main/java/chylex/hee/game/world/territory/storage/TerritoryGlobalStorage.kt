package chylex.hee.game.world.territory.storage
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryInstance.Companion.THE_HUB_INSTANCE
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.NBTObjectList
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.perSavefileData
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.storage.WorldSavedData
import java.util.EnumMap

class TerritoryGlobalStorage(name: String) : WorldSavedData(name){
	companion object{
		fun get() = perSavefileData("HEE_TERRITORIES", ::TerritoryGlobalStorage)
	}
	
	// Instance
	
	private val spawnEntry = TerritoryEntry(this, THE_HUB_INSTANCE)
	private val territoryData = EnumMap(TerritoryType.values().filterNot { it.isSpawn }.associate { it to mutableListOf<TerritoryEntry>() })
	
	private fun makeEntry(territory: TerritoryType, index: Int): TerritoryEntry{
		return TerritoryEntry(this, TerritoryInstance(territory, index))
	}
	
	fun assignNewIndex(territory: TerritoryType): Int{
		if (territory.isSpawn){
			return 0
		}
		
		val list = territoryData[territory]!!
		val newIndex = list.size
		
		list.add(makeEntry(territory, newIndex))
		markDirty()
		
		return newIndex
	}
	
	fun forInstance(instance: TerritoryInstance): TerritoryEntry?{
		val (territory, index) = instance
		
		return if (territory.isSpawn)
			spawnEntry
		else
			territoryData[territory]!!.getOrNull(index)
	}
	
	// Serialization
	
	override fun writeToNBT(nbt: NBTTagCompound) = nbt.apply {
		setTag("[Spawn]", spawnEntry.serializeNBT())
		
		for((key, list) in territoryData){
			 setList(key.title, NBTObjectList.of(list.map(TerritoryEntry::serializeNBT)))
		}
	}
	
	override fun readFromNBT(nbt: NBTTagCompound) = with(nbt){
		spawnEntry.deserializeNBT(getCompoundTag("[Spawn]"))
		
		for(key in keySet){
			val territory = TerritoryType.fromTitle(key) ?: continue
			val list = territoryData[territory]!!
			
			list.clear()
			list.addAll(getListOfCompounds(key).mapIndexed { index, nbt -> makeEntry(territory, index).also { it.deserializeNBT(nbt) } })
		}
		
		isDirty = false
	}
}
