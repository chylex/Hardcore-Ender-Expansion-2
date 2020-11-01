package chylex.hee.game.world.territory.storage
import chylex.hee.game.world.territory.storage.data.VoidData
import chylex.hee.system.serialization.TagCompound
import net.minecraftforge.common.util.INBTSerializable

abstract class TerritoryStorageComponent : INBTSerializable<TagCompound>{
	companion object{
		val VOID_DATA = VoidData::class.java to "Void"
		
		private val CLASS_TO_STRING = mapOf<Class<out TerritoryStorageComponent>, String>(
			VOID_DATA
		)
		
		private val STRING_TO_INSTANCE = mapOf<String, (() -> Unit) -> TerritoryStorageComponent>(
			"Void" to ::VoidData
		)
		
		fun getComponentName(component: TerritoryStorageComponent): String?{
			return CLASS_TO_STRING[component.javaClass]
		}
		
		fun getComponentConstructor(name: String): ((() -> Unit) -> TerritoryStorageComponent)?{
			return STRING_TO_INSTANCE[name]
		}
	}
}
