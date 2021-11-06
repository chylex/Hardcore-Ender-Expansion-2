package chylex.hee.datagen.client

import chylex.hee.datagen.client.util.block
import chylex.hee.datagen.client.util.layers
import chylex.hee.datagen.client.util.override
import chylex.hee.datagen.client.util.parent
import chylex.hee.datagen.client.util.simple
import chylex.hee.datagen.client.util.suffixed
import chylex.hee.datagen.then
import chylex.hee.game.Resource
import chylex.hee.game.Resource.location
import chylex.hee.game.block.IHeeBlock
import chylex.hee.game.item.IHeeItem
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.properties.ItemModel.AsBlock
import chylex.hee.game.item.properties.ItemModel.Copy
import chylex.hee.game.item.properties.ItemModel.FromParent
import chylex.hee.game.item.properties.ItemModel.Layers
import chylex.hee.game.item.properties.ItemModel.Manual
import chylex.hee.game.item.properties.ItemModel.Multi
import chylex.hee.game.item.properties.ItemModel.Named
import chylex.hee.game.item.properties.ItemModel.Simple
import chylex.hee.game.item.properties.ItemModel.SingleItemModel
import chylex.hee.game.item.properties.ItemModel.Skull
import chylex.hee.game.item.properties.ItemModel.SpawnEgg
import chylex.hee.game.item.properties.ItemModel.Suffixed
import chylex.hee.game.item.properties.ItemModel.Wall
import chylex.hee.game.item.properties.ItemModel.WithOverrides
import chylex.hee.game.item.properties.ItemModel.WithTextures
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import net.minecraft.block.Block
import net.minecraft.data.DataGenerator
import net.minecraft.util.IItemProvider
import net.minecraftforge.client.model.generators.ItemModelBuilder
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.data.ExistingFileHelper

class ItemModels(generator: DataGenerator, modid: String, existingFileHelper: ExistingFileHelper) : ItemModelProvider(generator, modid, existingFileHelper) {
	override fun registerModels() {
		for (item in ModItems.ALL) {
			(item as? IHeeItem)?.let { registerModel(item, it.model) }
		}
		
		for (block in ModBlocks.ALL) {
			(block as? IHeeBlock)
				?.let { it.model.generate(block).itemModel }
				?.let { registerModel(if (it.asItem) block.asItem() else block, it.model) }
		}
	}
	
	private fun registerModel(item: IItemProvider, model: ItemModel) {
		registerModel(item, model) { it }
	}
	
	private fun registerModel(item: IItemProvider, model: ItemModel, callback: (ItemModelBuilder) -> ItemModelBuilder) {
		when (model) {
			is SingleItemModel -> registerSingleModel(item, model, callback)
			
			is WithTextures -> registerModel(item, model.baseModel) {
				model.textures.entries.fold(callback(it)) { builder, (name, location) -> builder.texture(name, location) }
			}
			
			is WithOverrides -> registerModel(item, model.baseModel) {
				var builder = callback(it)
				
				for ((property, valueMap) in model.overrides) {
					for ((value, overrideModel) in valueMap) {
						registerSingleModel(item, overrideModel)
						builder = builder.override(overrideModel.getLocation(item.asItem())) {
							predicate(property.name, value)
						}
					}
				}
				
				builder
			}
			
			is Multi -> {
				for (innerModel in model.models) {
					registerModel(item, innerModel, callback)
				}
			}
		}
	}
	
	private fun registerSingleModel(item: IItemProvider, model: SingleItemModel) {
		registerSingleModel(item, model) { it }
	}
	
	private fun registerSingleModel(item: IItemProvider, model: SingleItemModel, callback: (ItemModelBuilder) -> ItemModelBuilder) {
		when (model) {
			Manual        -> return
			Simple        -> simple(item)?.then(callback)
			AsBlock       -> block(item as Block)?.then(callback)
			Skull         -> parent(item, Resource.Vanilla("item/template_skull"))?.then(callback)
			SpawnEgg      -> parent(item, Resource.Vanilla("item/template_spawn_egg"))?.then(callback)
			Wall          -> parent(item, item.suffixed("_inventory").location)?.then(callback)
			is Copy       -> simple(item, model.item.location)?.then(callback)
			is Layers     -> layers(item, model.layers)?.then(callback)
			is Named      -> simple(model.name)?.then(callback)
			is FromParent -> parent(item, model.parent)?.then(callback)
			is Suffixed   -> registerModel(item.suffixed(model.suffix), model.wrapped, callback)
		}
	}
}
