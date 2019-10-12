package chylex.hee.game.mechanics.table.process.serializer
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcessSerializer
import chylex.hee.system.util.TagCompound
import net.minecraft.world.World

class MultiProcessSerializer(private vararg val mappings: Mapping) : ITableProcessSerializer{
	companion object{
		inline fun <reified T : ITableProcess> Mapping(key: String, noinline constructor: (World, TagCompound) -> T): Mapping{
			return Mapping(key, T::class.java, BasicProcessSerializer(constructor))
		}
		
		private const val TYPE_TAG = "Type"
		private const val DATA_TAG = "Data"
	}
	
	class Mapping(val key: String, val cls: Class<out ITableProcess>, val serializer: ITableProcessSerializer)
	
	override fun writeToNBT(process: ITableProcess) = TagCompound().apply {
		val mapping = mappings.first { it.cls.isAssignableFrom(process.javaClass) }
		
		setString(TYPE_TAG, mapping.key)
		setTag(DATA_TAG, mapping.serializer.writeToNBT(process))
	}
	
	override fun readFromNBT(world: World, nbt: TagCompound): ITableProcess{
		return mappings
			.first { it.key == nbt.getString(TYPE_TAG) }
			.serializer
			.readFromNBT(world, nbt.getCompoundTag(DATA_TAG))
	}
}
