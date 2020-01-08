package chylex.hee.game.world.territory.storage
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryInstance.Companion.THE_HUB_INSTANCE
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.system.util.NBTList.Companion.putList
import chylex.hee.system.util.NBTObjectList
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.perDimensionData
import chylex.hee.system.util.use
import net.minecraft.world.storage.WorldSavedData
import java.util.EnumMap

class TerritoryGlobalStorage private constructor() : WorldSavedData(NAME){
	companion object{
		fun get() = TerritoryInstance.endWorld.perDimensionData(NAME, ::TerritoryGlobalStorage)
		
		private const val NAME = "HEE_TERRITORIES"
	}
	
	// Instance
	
	private val spawnEntry = TerritoryEntry(this, THE_HUB_INSTANCE)
	private val territoryData = EnumMap(TerritoryType.values().filterNot { it.isSpawn }.associateWith { mutableListOf<TerritoryEntry>() })
	
	private fun makeEntry(territory: TerritoryType, index: Int): TerritoryEntry{
		return TerritoryEntry(this, TerritoryInstance(territory, index))
	}
	
	fun assignNewIndex(territory: TerritoryType): Int{
		if (territory.isSpawn){
			return 0
		}
		
		val list = territoryData.getValue(territory)
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
			territoryData.getValue(territory).getOrNull(index)
	}
	
	// Serialization
	
	override fun write(nbt: TagCompound) = nbt.apply {
		put("[Spawn]", spawnEntry.serializeNBT())
		
		for((key, list) in territoryData){
			putList(key.title, NBTObjectList.of(list.map(TerritoryEntry::serializeNBT)))
		}
	}
	
	override fun read(nbt: TagCompound) = nbt.use {
		spawnEntry.deserializeNBT(getCompound("[Spawn]"))
		
		for(key in keySet()){
			val territory = TerritoryType.fromTitle(key) ?: continue
			val list = territoryData.getValue(territory)
			
			list.clear()
			list.addAll(getListOfCompounds(key).mapIndexed { index, nbt -> makeEntry(territory, index).also { it.deserializeNBT(nbt) } })
		}
		
		isDirty = false
	}
}
