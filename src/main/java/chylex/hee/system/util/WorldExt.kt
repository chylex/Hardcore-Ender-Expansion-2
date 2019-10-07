package chylex.hee.system.util
import net.minecraft.world.World
import net.minecraft.world.storage.WorldSavedData
import net.minecraftforge.fml.common.FMLCommonHandler

// Renaming

val World.totalTime
	get() = this.totalWorldTime

// World data

inline fun <reified T : WorldSavedData> World.perDimensionData(name: String, constructor: (String) -> T): T{
	return this.perWorldStorage.let {
		it.getOrLoadData(T::class.java, name) as? T ?: constructor(name).also { data -> it.setData(name, data) }
	}
}

inline fun <reified T : WorldSavedData> World.perSavefileData(name: String, constructor: (String) -> T): T{
	return this.mapStorage!!.let {
		it.getOrLoadData(T::class.java, name) as? T ?: constructor(name).also { data -> it.setData(name, data) }
	}
}

inline fun <reified T : WorldSavedData> perSavefileData(name: String, constructor: (String) -> T): T{
	return FMLCommonHandler.instance().minecraftServerInstance.entityWorld.perSavefileData(name, constructor)
}
