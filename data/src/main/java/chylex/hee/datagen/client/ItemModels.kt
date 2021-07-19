package chylex.hee.datagen.client

import chylex.hee.datagen.client.util.layers
import chylex.hee.datagen.client.util.override
import chylex.hee.datagen.client.util.parent
import chylex.hee.datagen.client.util.simple
import chylex.hee.datagen.client.util.suffixed
import chylex.hee.datagen.r
import chylex.hee.game.Resource
import chylex.hee.game.item.IHeeItem
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.properties.ItemModel.Multi
import chylex.hee.game.item.properties.ItemModel.SingleItemModel
import chylex.hee.game.item.properties.ItemModel.SingleItemModel.Copy
import chylex.hee.game.item.properties.ItemModel.SingleItemModel.Layers
import chylex.hee.game.item.properties.ItemModel.SingleItemModel.Named
import chylex.hee.game.item.properties.ItemModel.SingleItemModel.Simple
import chylex.hee.game.item.properties.ItemModel.SingleItemModel.Skull
import chylex.hee.game.item.properties.ItemModel.SingleItemModel.SpawnEgg
import chylex.hee.game.item.properties.ItemModel.SingleItemModel.Suffixed
import chylex.hee.game.item.properties.ItemModel.WithOverrides
import chylex.hee.init.ModItems
import chylex.hee.system.getRegistryEntries
import net.minecraft.data.DataGenerator
import net.minecraft.item.Item
import net.minecraftforge.client.model.generators.ItemModelBuilder
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.data.ExistingFileHelper

class ItemModels(generator: DataGenerator, modid: String, existingFileHelper: ExistingFileHelper) : ItemModelProvider(generator, modid, existingFileHelper) {
	override fun registerModels() {
		for (item in getRegistryEntries<Item>(ModItems)) {
			(item as? IHeeItem)?.model?.let { registerModel(item, it) {} }
		}
	}
	
	private fun registerModel(item: Item, model: ItemModel, callback: ItemModelBuilder.() -> Unit) {
		when (model) {
			is SingleItemModel -> registerSingleModel(item, model, callback)
			
			is Multi -> {
				for (it in model.models) {
					registerModel(item, it, callback)
				}
			}
			
			is WithOverrides -> {
				registerModel(item, model.baseModel) {
					callback()
					for ((propertyKey, valueMap) in model.overrides) {
						for ((propertyValue, overrideModel) in valueMap) {
							registerSingleModel(item, overrideModel) {}
							override(overrideModel.getLocation(item)) {
								predicate(propertyKey, propertyValue)
							}
						}
					}
				}
			}
		}
	}
	
	private fun registerSingleModel(item: Item, model: SingleItemModel, callback: ItemModelBuilder.() -> Unit) {
		when (model) {
			Simple      -> simple(item)?.let(callback)
			Skull       -> parent(item, Resource.Vanilla("item/template_skull"))?.let(callback)
			SpawnEgg    -> parent(item, Resource.Vanilla("item/template_spawn_egg"))?.let(callback)
			is Copy     -> simple(item, model.item.r)?.let(callback)
			is Layers   -> layers(item, model.layers)?.let(callback)
			is Named    -> simple(model.name)?.let(callback)
			is Suffixed -> registerModel(item.suffixed(model.suffix), model.wrapped, callback)
		}
	}
}
