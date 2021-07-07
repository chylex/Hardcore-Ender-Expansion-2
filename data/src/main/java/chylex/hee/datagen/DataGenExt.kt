package chylex.hee.datagen

import chylex.hee.HEE
import chylex.hee.game.Resource
import net.minecraft.block.Block
import net.minecraft.data.IDataProvider
import net.minecraft.item.Item
import net.minecraft.util.IItemProvider
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.generators.ModelBuilder
import net.minecraftforge.client.model.generators.ModelProvider
import net.minecraftforge.registries.IForgeRegistryEntry

val IForgeRegistryEntry<*>.path: String
	get() = registryName!!.path

val IForgeRegistryEntry<*>.isVanilla
	get() = Resource.isVanilla(registryName!!)

val IItemProvider.r
	get() = when (this) {
		is Block -> resource("block/" + this.path, this.isVanilla)
		is Item  -> resource("item/" + this.path, this.isVanilla)
		else     -> throw IllegalArgumentException()
	}

fun IItemProvider.r(suffix: String): ResourceLocation {
	return when (this) {
		is Block -> resource("block/" + this.path + suffix, this.isVanilla)
		is Item  -> resource("item/" + this.path + suffix, this.isVanilla)
		else     -> throw IllegalArgumentException()
	}
}

fun resource(path: String, vanilla: Boolean): ResourceLocation {
	return if (vanilla) Resource.Vanilla(path) else Resource.Custom(path)
}

inline fun <T : IDataProvider> T?.safeUnit(callback: T.() -> Unit) {
	try {
		this?.callback()
	} catch (e: Exception) {
		HEE.log.error("[DataGen] " + e.message)
	}
}

inline fun <T : ModelBuilder<T>, U : ModelProvider<T>> U?.safeUnit(callback: U.() -> Unit) {
	try {
		this?.callback()
	} catch (e: Exception) {
		HEE.log.error("[DataGen] " + e.message)
	}
}

inline fun <T : ModelBuilder<T>, U : ModelProvider<T>> U?.safe(callback: U.() -> T): T? {
	return try {
		this?.callback()
	} catch (e: Exception) {
		HEE.log.error("[DataGen] " + e.message)
		null
	}
}

inline fun <T : ModelBuilder<T>> T?.then(callback: T.() -> T): T? {
	return try {
		this?.callback()
	} catch (e: Exception) {
		HEE.log.error("[DataGen] " + e.message)
		null
	}
}

class Callback<T>(val item: T, val suffix: String, val path: String) {
	override fun toString() = path
}
