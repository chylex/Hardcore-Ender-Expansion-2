package chylex.hee.system.util
import net.minecraft.world.World
import net.minecraft.world.storage.WorldSavedData

inline fun <reified T : WorldSavedData> World.perDimensionData(name: String, constructor: (String) -> T): T{
	return this.perWorldStorage.let {
		it.getOrLoadData(T::class.java, name) as? T ?: constructor(name).also { data -> it.setData(name, data) }
	}
}
