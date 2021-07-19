package chylex.hee.datagen.client.util

import chylex.hee.datagen.safe
import chylex.hee.game.Resource
import chylex.hee.game.Resource.location
import chylex.hee.game.Resource.locationPrefix
import chylex.hee.system.named
import chylex.hee.system.path
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.IItemProvider
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.generators.ItemModelBuilder
import net.minecraftforge.client.model.generators.ItemModelBuilder.OverrideBuilder
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile

fun Item.suffixed(suffix: String): Item {
	return Item(Item.Properties()) named this.path + suffix
}

fun IItemProvider.suffixed(suffix: String): IItemProvider = when (this) {
	is Block -> this.suffixed(suffix)
	is Item  -> this.suffixed(suffix)
	else     -> throw IllegalArgumentException()
}

private val ItemModelProvider.generated
	get() = getExistingFile(Resource.Vanilla("item/generated"))

private fun ItemModelProvider.build(item: IItemProvider): ItemModelBuilder {
	return this.getBuilder(when (item) {
		is Block -> item.path
		is Item  -> item.path
		else     -> throw IllegalArgumentException()
	})
}

fun ItemModelProvider.parent(item: IItemProvider, parent: ResourceLocation) = safe {
	this.build(item).parent(getExistingFile(parent))
}

fun ItemModelProvider.simple(path: String, texture: String = "item/$path") = safe {
	this.getBuilder(path).parent(generated).texture("layer0", texture)
}

fun ItemModelProvider.simple(item: IItemProvider, texture: ResourceLocation = item.location) = safe {
	this.build(item).parent(generated).texture("layer0", texture)
}

fun ItemModelProvider.layers(item: IItemProvider, layers: Array<out String>) = safe {
	var builder = this.build(item).parent(generated)
	
	for ((index, layer) in layers.withIndex()) {
		builder = builder.texture("layer$index", Resource.Custom(item.locationPrefix + layer))
	}
	
	builder
}

fun ItemModelProvider.block(block: Block, parent: Block = block) = safe {
	this.getBuilder(block.path).parent(UncheckedModelFile(parent.location))
}

inline fun ItemModelBuilder.override(model: ResourceLocation, callback: OverrideBuilder.() -> OverrideBuilder): ItemModelBuilder {
	return this.override().model(UncheckedModelFile(model)).let(callback).end()
}
