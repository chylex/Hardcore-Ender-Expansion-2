package chylex.hee.system.capability
import chylex.hee.system.util.NBTBase
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.common.util.LazyOptional

abstract class CapabilityProvider<T : INBTSerializable<U>, U : NBTBase>(
	instance: Capability<T>?,
	private val impl: T
) : ICapabilitySerializable<U>{
	private val instance = instance!!
	
	@Suppress("UNCHECKED_CAST")
	override fun <T : Any?> getCapability(capability: Capability<T>, facing: Direction?): LazyOptional<T>{
		return if (capability === instance)
			LazyOptional.of { impl }.cast()
		else
			LazyOptional.empty()
	}
	
	override fun serializeNBT(): U{
		return impl.serializeNBT()
	}
	
	override fun deserializeNBT(nbt: U){
		impl.deserializeNBT(nbt)
	}
}
