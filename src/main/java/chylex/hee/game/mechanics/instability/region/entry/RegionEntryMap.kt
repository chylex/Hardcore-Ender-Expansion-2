package chylex.hee.game.mechanics.instability.region.entry
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import net.minecraft.nbt.NBTTagLongArray
import net.minecraftforge.common.util.INBTSerializable

class RegionEntryMap<T : IRegionEntry>(private val constructor: IRegionEntryConstructor<T>) : INBTSerializable<NBTTagLongArray>{
	private companion object{
		private const val MISSING = Long.MAX_VALUE
	}
	
	private val entries = Long2LongOpenHashMap().apply { defaultReturnValue(MISSING) }
	
	val isEmpty
		get() = entries.isEmpty()
	
	val regions
		get() = entries.keys.map(constructor::fromCompacted)
	
	private fun addOrUpdateEntry(entry: IRegionEntry){
		entries.put(entry.key, entry.compacted) // kotlin indexer boxes the values
	}
	
	fun containsRegion(entry: T): Boolean{
		return entries.containsKey(entry.key)
	}
	
	fun removeRegion(entry: T){
		entries.remove(entry.key)
	}
	
	fun setPoints(entry: T, points: Int){
		addOrUpdateEntry(entry.withPoints(points))
	}
	
	fun getPoints(entry: T): Int{
		return when(val compacted = entries[entry.key]){
			MISSING -> 0
			else -> constructor.fromCompacted(compacted).points
		}
	}
	
	override fun serializeNBT(): NBTTagLongArray{
		return NBTTagLongArray(entries.values.toLongArray())
	}
	
	override fun deserializeNBT(nbt: NBTTagLongArray?){
		entries.clear()
		
		if (nbt != null){
			for(compacted in nbt.data){
				addOrUpdateEntry(constructor.fromCompacted(compacted))
			}
		}
	}
}
