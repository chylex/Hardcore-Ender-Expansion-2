package chylex.hee.game.world.territory.storage
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.perDimensionData
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryInstance.Companion.THE_HUB_INSTANCE
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.system.serialization.NBTList.Companion.putList
import chylex.hee.system.serialization.NBTObjectList
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getListOfCompounds
import chylex.hee.system.serialization.use
import net.minecraft.world.storage.WorldSavedData
import java.util.EnumMap

class TerritoryGlobalStorage private constructor() : WorldSavedData(NAME){
	companion object{
		fun get() = TerritoryInstance.endWorld.perDimensionData(NAME, ::TerritoryGlobalStorage)
		
		private const val NAME = "HEE_TERRITORIES"
	}
	
	// Instance
	
	private val spawnEntry = TerritoryEntry(this, THE_HUB_INSTANCE, TokenType.NORMAL)
	private val territoryData = EnumMap(TerritoryType.values().filterNot { it.isSpawn }.associateWith { mutableListOf<TerritoryEntry>() })
	
	private fun makeEntry(territory: TerritoryType, index: Int, type: TokenType): TerritoryEntry{
		return TerritoryEntry(this, TerritoryInstance(territory, index), type)
	}
	
	private fun makeEntry(territory: TerritoryType, index: Int, tag: TagCompound): TerritoryEntry{
		return TerritoryEntry.fromTag(this, TerritoryInstance(territory, index), tag)
	}
	
	fun assignNewIndex(territory: TerritoryType, tokenType: TokenType): Int{
		if (territory.isSpawn){
			return 0
		}
		
		val list = territoryData.getValue(territory)
		val newIndex = list.size
		
		list.add(makeEntry(territory, newIndex, tokenType))
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
			list.addAll(getListOfCompounds(key).mapIndexed { index, nbt -> makeEntry(territory, index, nbt) })
		}
		
		isDirty = false
	}
}
