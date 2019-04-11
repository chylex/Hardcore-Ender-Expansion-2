package chylex.hee.system.util
import chylex.hee.system.capability.NullFactory
import chylex.hee.system.capability.NullStorage
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.ICapabilityProvider
import java.util.concurrent.Callable

inline fun <reified T> CapabilityManager.register(storage: IStorage<T> = NullStorage.get(), factory: Callable<out T> = NullFactory.get()){
	this.register(T::class.java, storage, factory)
}

fun <T> ICapabilityProvider.hasCap(capability: Capability<T>?, facing: EnumFacing? = null): Boolean{
	return this.hasCapability(capability!!, facing)
}

fun <T> ICapabilityProvider.getCap(capability: Capability<T>?, facing: EnumFacing? = null): T{
	return this.getCapability(capability!!, facing)!!
}

fun <T> ICapabilityProvider.getCapOrNull(capability: Capability<T>?, facing: EnumFacing? = null): T?{
	return this.getCapability(capability!!, facing)
}
