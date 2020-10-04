package chylex.hee.datagen.server.util
import net.minecraft.tags.Tag

fun <T> Tag.Builder<T>.add(items: List<T>){
	items.forEach(this::add)
}
