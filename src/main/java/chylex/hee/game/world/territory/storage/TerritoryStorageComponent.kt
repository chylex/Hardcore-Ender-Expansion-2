package chylex.hee.game.world.territory.storage
import chylex.hee.system.serialization.TagCompound
import net.minecraftforge.common.util.INBTSerializable

abstract class TerritoryStorageComponent : INBTSerializable<TagCompound>{
	companion object{
		private val CLASS_TO_STRING = mapOf<Class<out TerritoryStorageComponent>, String>(
		)
		
		private val STRING_TO_INSTANCE = mapOf<String, (() -> Unit) -> TerritoryStorageComponent>(
		)
		
		fun getComponentName(component: TerritoryStorageComponent): String?{
			return CLASS_TO_STRING[component.javaClass]
		}
		
		fun getComponentConstructor(name: String): ((() -> Unit) -> TerritoryStorageComponent)?{
			return STRING_TO_INSTANCE[name]
		}
	}
}
