package chylex.hee.game.mechanics.table.process.serializer
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcessSerializer
import chylex.hee.system.serialization.TagCompound

class MultiProcessSerializer(private vararg val mappings: Mapping) : ITableProcessSerializer{
	companion object{
		inline fun <reified T : ITableProcess> Mapping(key: String, noinline constructor: (TileEntityBaseTable, TagCompound) -> T): Mapping{
			return Mapping(key, T::class.java, BasicProcessSerializer(constructor))
		}
		
		private const val TYPE_TAG = "Type"
		private const val DATA_TAG = "Data"
	}
	
	class Mapping(val key: String, val cls: Class<out ITableProcess>, val serializer: ITableProcessSerializer)
	
	override fun writeToNBT(process: ITableProcess) = TagCompound().apply {
		val mapping = mappings.first { it.cls.isAssignableFrom(process.javaClass) }
		
		putString(TYPE_TAG, mapping.key)
		put(DATA_TAG, mapping.serializer.writeToNBT(process))
	}
	
	override fun readFromNBT(table: TileEntityBaseTable, nbt: TagCompound): ITableProcess{
		return mappings
			.first { it.key == nbt.getString(TYPE_TAG) }
			.serializer
			.readFromNBT(table, nbt.getCompound(DATA_TAG))
	}
}
