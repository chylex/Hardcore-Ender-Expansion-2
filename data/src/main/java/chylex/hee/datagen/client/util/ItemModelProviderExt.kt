package chylex.hee.datagen.client.util

import chylex.hee.datagen.Callback
import chylex.hee.datagen.path
import chylex.hee.datagen.r
import chylex.hee.datagen.safe
import chylex.hee.datagen.safeUnit
import chylex.hee.game.Resource
import chylex.hee.system.named
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

private val ItemModelProvider.generated
	get() = getExistingFile(Resource.Vanilla("item/generated"))

private fun IItemProvider.path() = when (this) {
	is Block -> this.path
	is Item  -> this.path
	else     -> throw IllegalArgumentException()
}

private fun ItemModelProvider.build(item: IItemProvider): ItemModelBuilder {
	return this.getBuilder(item.path())
}

fun ItemModelProvider.parent(item: IItemProvider, parent: ResourceLocation, checkExistence: Boolean = true) = safe {
	this.build(item).parent(if (checkExistence) getExistingFile(parent) else UncheckedModelFile(parent))
}

fun ItemModelProvider.simple(path: String, texture: String = "item/$path") = safe {
	this.getBuilder(path).parent(generated).texture("layer0", texture)
}

fun ItemModelProvider.simple(item: IItemProvider, texture: ResourceLocation = item.r) = safe {
	this.build(item).parent(generated).texture("layer0", texture)
}

fun ItemModelProvider.layers(item: Item, layers: Array<String>) = safe {
	var builder = this.getBuilder(item.path).parent(generated)
	
	for ((index, layer) in layers.withIndex()) {
		builder = builder.texture("layer$index", Resource.Custom("item/$layer"))
	}
	
	builder
}

fun ItemModelProvider.multi(item: IItemProvider, parent: ResourceLocation, suffixes: Array<String>, callback: ItemModelBuilder.(Callback<IItemProvider>) -> Unit) {
	for (suffix in suffixes) {
		val path = item.path() + suffix
		
		this.safeUnit {
			this.getBuilder(path).parent(getExistingFile(parent)).callback(Callback(item, suffix, path))
		}
	}
}

fun ItemModelProvider.multi(item: IItemProvider, parent: ResourceLocation, suffixes: IntRange, callback: ItemModelBuilder.(Callback<IItemProvider>) -> Unit) {
	multi(item, parent, Array(1 + suffixes.last - suffixes.first) { "_${suffixes.first + it}" }, callback)
}

fun ItemModelProvider.block(block: Block, parent: Block = block) = safe {
	this.getBuilder(block.path).parent(UncheckedModelFile(parent.r))
}

fun ItemModelBuilder.override(model: ResourceLocation, callback: OverrideBuilder.() -> OverrideBuilder): ItemModelBuilder? {
	return this.override().model(UncheckedModelFile(model)).let(callback).end()
}
