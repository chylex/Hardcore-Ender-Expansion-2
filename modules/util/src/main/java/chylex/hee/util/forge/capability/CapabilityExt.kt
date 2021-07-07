package chylex.hee.util.forge.capability

import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import java.util.concurrent.Callable

inline fun <reified T> CapabilityManager.register(storage: IStorage<T> = NullStorage.get(), factory: Callable<out T> = NullFactory.get()) {
	this.register(T::class.java, storage, factory)
}

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun <T> ICapabilityProvider.getCap(capability: Capability<T>?, facing: Direction? = null): T {
	return this.getCapability(capability!!, facing).orElse(null) ?: throw NullPointerException()
}

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun <T> ICapabilityProvider.getCapOrNull(capability: Capability<T>?, facing: Direction? = null): T? {
	return this.getCapability(capability!!, facing).orElse(null)
}

fun <T : Any> LazyOptional(impl: T): LazyOptional<T> {
	return LazyOptional.of { impl }
}
