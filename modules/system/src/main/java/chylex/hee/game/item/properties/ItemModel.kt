package chylex.hee.game.item.properties

import chylex.hee.game.Resource
import chylex.hee.system.isVanilla
import chylex.hee.system.path
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation

sealed class ItemModel {
	companion object {
		val Simple   = SingleItemModel.Simple
		val Skull    = SingleItemModel.Skull
		val SpawnEgg = SingleItemModel.SpawnEgg
		
		fun Layers(vararg layers: String)                         = SingleItemModel.Layers(layers)
		fun Copy(item: Item)                                      = SingleItemModel.Copy(item)
		fun Named(name: String)                                   = SingleItemModel.Named(name)
		fun Suffixed(suffix: String, wrapped: ItemModel = Simple) = SingleItemModel.Suffixed(suffix, wrapped)
		
		private fun resource(path: String, vanilla: Boolean): ResourceLocation {
			return if (vanilla) Resource.Vanilla(path) else Resource.Custom(path)
		}
	}
	
	sealed class SingleItemModel : ItemModel() {
		open fun getLocation(item: Item): ResourceLocation {
			return resource("item/" + item.path, item.isVanilla)
		}
		
		object Simple : SingleItemModel()
		object Skull : SingleItemModel()
		object SpawnEgg : SingleItemModel()
		
		class Layers(val layers: Array<out String>) : SingleItemModel()
		
		data class Copy(val item: Item) : SingleItemModel()
		
		data class Named(val name: String) : SingleItemModel() {
			override fun getLocation(item: Item): ResourceLocation {
				return resource("item/$name", item.isVanilla)
			}
		}
		
		class Suffixed(val suffix: String, val wrapped: ItemModel) : SingleItemModel() {
			override fun getLocation(item: Item): ResourceLocation {
				return resource("item/" + item.path + suffix, item.isVanilla)
			}
		}
	}
	
	class Multi(vararg val models: ItemModel) : ItemModel()
	class WithOverrides(val baseModel: ItemModel, vararg val overrides: Pair<ResourceLocation, Map<Float, SingleItemModel>>) : ItemModel()
}
