package chylex.hee.game.mechanics.table.process.serializer
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.interfaces.ITableProcessSerializer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World

class MultiProcessSerializer(private vararg val mappings: Mapping) : ITableProcessSerializer{
	companion object{
		inline fun <reified T : ITableProcess> Mapping(key: String, noinline constructor: (World, NBTTagCompound) -> T): Mapping{
			return Mapping(key, T::class.java, BasicProcessSerializer(constructor))
		}
	}
	
	class Mapping(val key: String, val cls: Class<out ITableProcess>, val serializer: ITableProcessSerializer)
	
	override fun writeToNBT(process: ITableProcess) = NBTTagCompound().apply {
		val mapping = mappings.first { it.cls.isAssignableFrom(process.javaClass) }
		
		setString("Type", mapping.key)
		setTag("Data", mapping.serializer.writeToNBT(process))
	}
	
	override fun readFromNBT(world: World, nbt: NBTTagCompound): ITableProcess{
		return mappings
			.first { it.key == nbt.getString("Type") }
			.serializer
			.readFromNBT(world, nbt.getCompoundTag("Data"))
	}
}
