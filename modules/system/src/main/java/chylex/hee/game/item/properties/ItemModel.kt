package chylex.hee.game.item.properties

import chylex.hee.game.Resource
import chylex.hee.game.item.util.ItemProperty
import chylex.hee.system.isVanilla
import chylex.hee.system.path
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.IItemProvider
import net.minecraft.util.ResourceLocation

sealed class ItemModel {
	sealed class SingleItemModel : ItemModel() {
		open fun getLocation(item: Item): ResourceLocation {
			return Resource("item/" + item.path, item.isVanilla)
		}
	}
	
	object Manual : SingleItemModel()
	object Simple : SingleItemModel()
	object AsBlock : SingleItemModel()
	object Skull : SingleItemModel()
	object SpawnEgg : SingleItemModel()
	object Wall : SingleItemModel()
	
	class Layers(vararg val layers: String) : SingleItemModel()
	class Copy(val item: IItemProvider) : SingleItemModel()
	
	class Named(val name: String) : SingleItemModel() {
		override fun getLocation(item: Item): ResourceLocation {
			return Resource("item/$name", item.isVanilla)
		}
	}
	
	class FromParent(val parent: ResourceLocation) : SingleItemModel() {
		constructor(block: Block) : this(Resource("block/" + block.path, block.isVanilla))
		
		override fun getLocation(item: Item): ResourceLocation {
			return parent
		}
	}
	
	class Suffixed(val suffix: String, val wrapped: ItemModel = Simple) : SingleItemModel() {
		override fun getLocation(item: Item): ResourceLocation {
			return Resource("item/" + item.path + suffix, item.isVanilla)
		}
	}
	
	class WithTextures(val baseModel: ItemModel, val textures: Map<String, ResourceLocation>) : ItemModel()
	class WithOverrides(val baseModel: ItemModel, vararg val overrides: Pair<ItemProperty, Map<Float, SingleItemModel>>) : ItemModel()
	class Multi(vararg val models: ItemModel) : ItemModel()
}
