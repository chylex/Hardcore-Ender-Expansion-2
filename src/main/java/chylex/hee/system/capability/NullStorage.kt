package chylex.hee.system.capability
import chylex.hee.system.util.NBTBase
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage

object NullStorage : IStorage<Any>{
	@Suppress("UNCHECKED_CAST")
	fun <T> get(): IStorage<T> = this as IStorage<T>
	
	override fun writeNBT(capability: Capability<Any>, instance: Any, side: EnumFacing?): NBTBase? = null
	override fun readNBT(capability: Capability<Any>, instance: Any, side: EnumFacing?, nbt: NBTBase){}
}
