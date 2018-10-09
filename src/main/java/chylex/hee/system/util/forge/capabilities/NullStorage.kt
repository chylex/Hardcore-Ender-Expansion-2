package chylex.hee.system.util.forge.capabilities
import net.minecraft.nbt.NBTBase
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage

object NullStorage : IStorage<Any>{
	fun <T> get(): IStorage<T> = this as IStorage<T>
	
	override fun writeNBT(capability: Capability<Any>, instance: Any, side: EnumFacing?): NBTBase? = null
	override fun readNBT(capability: Capability<Any>, instance: Any, side: EnumFacing?, nbt: NBTBase){}
}
