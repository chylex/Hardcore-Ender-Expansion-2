package chylex.hee.system.util.forge.capabilities
import net.minecraft.nbt.NBTBase
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.INBTSerializable

abstract class CapabilityProvider<T : INBTSerializable<U>, U : NBTBase>(
	private val instance: Capability<T>,
	private val impl: T
) : ICapabilitySerializable<U>{
	override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean{
		return capability === instance
	}
	
	override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T?{
		return if (capability === instance)
			instance.cast(impl)
		else
			null
	}
	
	override fun serializeNBT(): U{
		return impl.serializeNBT()
	}
	
	override fun deserializeNBT(nbt: U){
		impl.deserializeNBT(nbt)
	}
}
