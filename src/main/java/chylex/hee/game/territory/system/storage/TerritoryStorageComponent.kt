package chylex.hee.game.territory.system.storage

import chylex.hee.game.territory.storage.ForgottenTombsEndData
import chylex.hee.game.territory.storage.VoidData
import chylex.hee.util.nbt.TagCompound
import net.minecraftforge.common.util.INBTSerializable

abstract class TerritoryStorageComponent : INBTSerializable<TagCompound> {
	companion object {
		private val CLASS_TO_STRING = mutableMapOf<Class<out TerritoryStorageComponent>, String>()
		private val STRING_TO_INSTANCE = mutableMapOf<String, (() -> Unit) -> TerritoryStorageComponent>()
		
		private inline fun <reified T : TerritoryStorageComponent> register(name: String, noinline constructor: (() -> Unit) -> T): Pair<Class<T>, String> {
			require(CLASS_TO_STRING.put(T::class.java, name) == null)
			require(STRING_TO_INSTANCE.put(name, constructor) == null)
			return T::class.java to name
		}
		
		val VOID_DATA = register("Void", ::VoidData)
		val FORGOTTEN_TOMBS_END_DATA = register("ForgottenTombsEnd", ::ForgottenTombsEndData)
		
		fun getComponentName(component: TerritoryStorageComponent): String? {
			return CLASS_TO_STRING[component.javaClass]
		}
		
		fun getComponentConstructor(name: String): ((() -> Unit) -> TerritoryStorageComponent)? {
			return STRING_TO_INSTANCE[name]
		}
	}
}
