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
	}
	
	class Mapping(val key: String, val cls: Class<out ITableProcess>, val serializer: ITableProcessSerializer)
	
	override fun writeToNBT(process: ITableProcess) = TagCompound().apply {
		val mapping = mappings.first { it.cls.isAssignableFrom(process.javaClass) }
		
		setString("Type", mapping.key)
		setTag("Data", mapping.serializer.writeToNBT(process))
	}
	
	override fun readFromNBT(world: World, nbt: TagCompound): ITableProcess{
		return mappings
			.first { it.key == nbt.getString("Type") }
			.serializer
			.readFromNBT(world, nbt.getCompoundTag("Data"))
	}
}
