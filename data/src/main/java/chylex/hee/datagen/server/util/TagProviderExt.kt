package chylex.hee.datagen.server.util

import net.minecraft.data.TagsProvider

fun <T> TagsProvider.Builder<T>.add(items: List<T>) {
	items.forEach(this::addItemEntry)
}
