package chylex.hee.util.forge.capability

import chylex.hee.util.nbt.NBTBase
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.common.util.LazyOptional

abstract class CapabilityProvider<T : INBTSerializable<U>, U : NBTBase>(
	instance: Capability<T>?,
	private val impl: T,
) : ICapabilitySerializable<U> {
	private val instance = instance!!
	private val cap = LazyOptional(impl)
	
	@Suppress("UNCHECKED_CAST")
	override fun <T : Any?> getCapability(capability: Capability<T>, facing: Direction?): LazyOptional<T> {
		return if (capability === instance)
			cap.cast()
		else
			LazyOptional.empty()
	}
	
	override fun serializeNBT(): U {
		return impl.serializeNBT()
	}
	
	override fun deserializeNBT(nbt: U) {
		impl.deserializeNBT(nbt)
	}
}
