package chylex.hee.datagen

import chylex.hee.HEE
import net.minecraft.data.IDataProvider
import net.minecraftforge.client.model.generators.ModelBuilder
import net.minecraftforge.client.model.generators.ModelProvider

inline fun <T : IDataProvider> T?.safeUnit(callback: T.() -> Unit) {
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
