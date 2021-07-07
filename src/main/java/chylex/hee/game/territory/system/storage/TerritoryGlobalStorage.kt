package chylex.hee.game.territory.system.storage

import chylex.hee.HEE
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.territory.TerritoryType
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.territory.system.TerritoryInstance.Companion.THE_HUB_INSTANCE
import chylex.hee.game.world.util.perDimensionData
import chylex.hee.util.nbt.NBTObjectList
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getListOfCompounds
import chylex.hee.util.nbt.putList
import chylex.hee.util.nbt.use
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.storage.WorldSavedData
import java.util.EnumMap
import java.util.UUID

class TerritoryGlobalStorage private constructor() : WorldSavedData(NAME) {
	companion object {
		fun get() = TerritoryInstance.endWorld.perDimensionData(NAME, ::TerritoryGlobalStorage)
		
		private const val NAME = "HEE_TERRITORIES"
		
		private const val SPAWN_TAG = "Spawn"
		private const val TERRITORIES_TAG = "Territories"
		private const val SOLITARY_TAG = "Solitary"
		
		private const val SOLITARY_MISSING = -1
	}
	
	// Instance
	
	private val spawnEntry = TerritoryEntry(this, THE_HUB_INSTANCE, TokenType.NORMAL)
	private val territoryData = EnumMap(TerritoryType.values().filterNot { it.isSpawn }.associateWith { mutableListOf<TerritoryEntry>() })
	private val solitaryPlayers = mutableMapOf<TerritoryInstance, Object2IntMap<UUID>>()
	
	private fun makeEntry(territory: TerritoryType, index: Int, type: TokenType): TerritoryEntry {
		return TerritoryEntry(this, TerritoryInstance(territory, index), type)
	}
	
	private fun makeEntry(territory: TerritoryType, index: Int, tag: TagCompound): TerritoryEntry {
		return TerritoryEntry.fromTag(this, TerritoryInstance(territory, index), tag)
	}
	
	fun assignNewIndex(territory: TerritoryType, tokenType: TokenType): Int {
		if (territory.isSpawn) {
			return 0
		}
		
		val list = territoryData.getValue(territory)
		val newIndex = list.size
		
		list.add(makeEntry(territory, newIndex, tokenType))
		markDirty()
		
		return newIndex
	}
	
	fun forInstance(instance: TerritoryInstance): TerritoryEntry? {
		val (territory, index) = instance
		
		return if (territory.isSpawn)
			spawnEntry
		else
			territoryData.getValue(territory).getOrNull(index)
	}
	
	fun remapSolitaryIndex(instance: TerritoryInstance, player: PlayerEntity): TerritoryInstance {
		val territory = instance.territory
		
		val players = solitaryPlayers.getOrPut(instance) { Object2IntOpenHashMap<UUID>().apply { defaultReturnValue(SOLITARY_MISSING) } }
		val remapped = players.getInt(player.uniqueID)
		
		if (remapped != SOLITARY_MISSING) {
			return TerritoryInstance(territory, remapped)
		}
		
		val newIndex = assignNewIndex(territory, TokenType.SOLITARY)
		
		@Suppress("ReplacePutWithAssignment")
		players.put(player.uniqueID, newIndex)
		markDirty()
		
		return TerritoryInstance(territory, newIndex)
	}
	
	// Serialization
	
	override fun write(nbt: TagCompound) = nbt.apply {
		put(SPAWN_TAG, spawnEntry.serializeNBT())
		
		put(TERRITORIES_TAG, TagCompound().apply {
			for ((key, list) in territoryData) {
				putList(key.title, NBTObjectList.of(list.map(TerritoryEntry::serializeNBT)))
			}
		})
		
		put(SOLITARY_TAG, TagCompound().apply {
			for ((instance, map) in solitaryPlayers) {
				val solitaryPlayersTag = TagCompound()
				
				for (entry in map.object2IntEntrySet()) {
					solitaryPlayersTag.putInt(entry.key.toString(), entry.intValue)
				}
				
				put(instance.hash.toString(), solitaryPlayersTag)
			}
		})
	}
	
	override fun read(nbt: TagCompound) = nbt.use {
		spawnEntry.deserializeNBT(getCompound(SPAWN_TAG))
		
		with(getCompound(TERRITORIES_TAG)) {
			for (key in keySet()) {
				val territory = TerritoryType.fromTitle(key) ?: continue
				val list = territoryData.getValue(territory)
				
				list.clear()
				list.addAll(getListOfCompounds(key).mapIndexed { index, nbt -> makeEntry(territory, index, nbt) })
			}
		}
		
		with(getCompound(SOLITARY_TAG)) {
			solitaryPlayers.clear()
			
			for (keyHash in keySet()) {
				val hash = keyHash.toIntOrNull()
				val instance = hash?.let(TerritoryInstance::fromHash)
				
				if (instance == null) {
					HEE.log.error("[TerritoryGlobalStorage] invalid solitary territory hash: $keyHash")
					continue
				}
				
				val solitaryPlayersMap = Object2IntOpenHashMap<UUID>().apply { defaultReturnValue(SOLITARY_MISSING) }
				val solitaryPlayersTag = getCompound(keyHash)
				
				for (keyUUID in solitaryPlayersTag.keySet()) {
					val uuid = try {
						UUID.fromString(keyUUID)
					} catch (e: Exception) {
						HEE.log.error("[TerritoryGlobalStorage] could not parse solitary player UUID: $keyUUID")
						continue
					}
					
					solitaryPlayersMap[uuid] = solitaryPlayersTag.getInt(keyUUID)
				}
				
				solitaryPlayers[instance] = solitaryPlayersMap
			}
		}
		
		isDirty = false
	}
}
