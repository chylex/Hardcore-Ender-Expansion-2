package chylex.hee.game.world.territory.storage
import chylex.hee.HEE
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.ChunkGeneratorEndCustom
import chylex.hee.game.world.Pos
import chylex.hee.game.world.center
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.system.delegate.NotifyOnChange
import chylex.hee.system.math.toYaw
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getCompoundOrNull
import chylex.hee.system.serialization.getEnum
import chylex.hee.system.serialization.getLongArrayOrNull
import chylex.hee.system.serialization.getPosOrNull
import chylex.hee.system.serialization.putEnum
import chylex.hee.system.serialization.putPos
import chylex.hee.system.serialization.use
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.INBTSerializable
import java.util.UUID

class TerritoryEntry(private val owner: TerritoryGlobalStorage, private val instance: TerritoryInstance, val type: TokenType) : INBTSerializable<TagCompound>{
	companion object{
		private const val TYPE_TAG = "Type"
		private const val SPAWN_POINT_TAG = "Spawn"
		private const val INTEREST_POINT_TAG = "Interest"
		private const val LAST_PORTALS_TAG = "LastPortals"
		private const val COMPONENTS_TAG = "Components"
		
		fun fromTag(owner: TerritoryGlobalStorage, instance: TerritoryInstance, tag: TagCompound): TerritoryEntry{
			return TerritoryEntry(owner, instance, tag.getEnum<TokenType>(TYPE_TAG) ?: TokenType.NORMAL).also { it.deserializeNBT(tag) }
		}
	}
	
	val markDirty = owner::markDirty
	
	private var spawnPoint: BlockPos? by NotifyOnChange(null, markDirty)
	private var interestPoint: BlockPos? by NotifyOnChange(null, markDirty)
	
	val spawnYaw
		get() = interestPoint?.center?.let { ip -> spawnPoint?.center?.let { sp -> ip.subtract(sp).toYaw() } }
	
	private val lastPortals = mutableMapOf<UUID, BlockPos>()
	private val components = mutableMapOf<Class<out TerritoryStorageComponent>, TerritoryStorageComponent>()
	
	fun loadSpawn(): BlockPos{
		if (spawnPoint == null){
			val info = (TerritoryInstance.endWorld.chunkProvider.chunkGenerator as ChunkGeneratorEndCustom).getGenerationInfo(instance)
			
			val startChunk = instance.topLeftChunk
			val bottomY = instance.territory.height.first
			
			spawnPoint = info.spawnPoint.let { startChunk.asBlockPos().add(it.x, bottomY + it.y, it.z) }
			interestPoint = info.interestPoint?.let { startChunk.asBlockPos().add(it.x, bottomY + it.y, it.z) }
		}
		
		return spawnPoint!!
	}
	
	fun loadSpawnForPlayer(player: EntityPlayer): BlockPos{
		return lastPortals[player.uniqueID] ?: loadSpawn()
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
	
	fun <T : TerritoryStorageComponent> registerComponent(info: Pair<Class<T>, String>): T{
		@Suppress("UNCHECKED_CAST")
		return components.getOrPut(info.first){ TerritoryStorageComponent.getComponentConstructor(info.second)!!(markDirty) } as T
	}
	
	fun <T : TerritoryStorageComponent> getComponent(cls: Class<T>): T?{
		@Suppress("UNCHECKED_CAST")
		return components[cls]?.let { it as T }
	}
	
	inline fun <reified T : TerritoryStorageComponent> getComponent(): T?{
		return getComponent(T::class.java)
	}
	
	override fun serializeNBT() = TagCompound().apply {
		if (this@TerritoryEntry.type != TokenType.NORMAL){
			putEnum(TYPE_TAG, this@TerritoryEntry.type)
		}
		
		spawnPoint?.let {
			putPos(SPAWN_POINT_TAG, it)
		}
		
		interestPoint?.let {
			putPos(INTEREST_POINT_TAG, it)
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
			
			putLongArray(LAST_PORTALS_TAG, lastPortalArray)
		}
		
		if (components.isNotEmpty()){
			val componentTag = TagCompound()
			
			for(component in components.values){
				val name = TerritoryStorageComponent.getComponentName(component)
				
				if (name == null){
					HEE.log.error("[TerritoryEntry] could not map storage component ${component.javaClass.name} to its name")
				}
				else{
					componentTag.put(name, component.serializeNBT())
				}
			}
			
			put(COMPONENTS_TAG, componentTag)
		}
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		spawnPoint = getPosOrNull(SPAWN_POINT_TAG)
		interestPoint = getPosOrNull(INTEREST_POINT_TAG)
		
		lastPortals.clear()
		
		val lastPortalArray = getLongArrayOrNull(LAST_PORTALS_TAG)
		
		if (lastPortalArray != null){
			for(index in lastPortalArray.indices step 3){
				lastPortals[UUID(lastPortalArray[index + 0], lastPortalArray[index + 1])] = Pos(lastPortalArray[2])
			}
		}
		
		components.clear()
		
		val componentTag = getCompoundOrNull(COMPONENTS_TAG)
		
		if (componentTag != null){
			for(name in componentTag.keySet()){
				val constructor = TerritoryStorageComponent.getComponentConstructor(name)
				
				if (constructor == null){
					HEE.log.error("[TerritoryEntry] could not map storage component name $name to its constructor")
				}
				else{
					val component = constructor(markDirty).also { it.deserializeNBT(componentTag.getCompound(name)) }
					components[component.javaClass] = component
				}
			}
		}
	}
}
