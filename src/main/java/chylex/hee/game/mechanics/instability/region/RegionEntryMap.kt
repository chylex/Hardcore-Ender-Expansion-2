package chylex.hee.game.mechanics.instability.region

import chylex.hee.util.nbt.TagLongArray
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import net.minecraftforge.common.util.INBTSerializable

class RegionEntryMap<T : IRegionEntry>(private val constructor: IRegionEntryConstructor<T>) : INBTSerializable<TagLongArray> {
	private companion object {
		private const val MISSING = Long.MAX_VALUE
	}
	
	private val entries = Long2LongOpenHashMap().apply { defaultReturnValue(MISSING) }
	
	val isEmpty
		get() = entries.isEmpty()
	
	val regions
		get() = entries.keys.map(constructor::fromCompacted)
	
	private fun addOrUpdateEntry(entry: IRegionEntry) {
		@Suppress("ReplacePutWithAssignment")
		entries.put(entry.key, entry.compacted) // kotlin indexer boxes the values
	}
	
	fun containsRegion(entry: T): Boolean {
		return entries.containsKey(entry.key)
	}
	
	fun removeRegion(entry: T) {
		entries.remove(entry.key)
	}
	
	fun setPoints(entry: T, points: Int) {
		addOrUpdateEntry(entry.withPoints(points))
	}
	
	fun getPoints(entry: T): Int {
		return when (val compacted = entries[entry.key]) {
			MISSING -> 0
			else    -> constructor.fromCompacted(compacted).points
		}
	}
	
	override fun serializeNBT(): TagLongArray {
		return TagLongArray(entries.values.toLongArray())
	}
	
	override fun deserializeNBT(nbt: TagLongArray?) {
		entries.clear()
		
		if (nbt != null) {
			for (compacted in nbt.asLongArray) {
				addOrUpdateEntry(constructor.fromCompacted(compacted))
			}
		}
	}
}
