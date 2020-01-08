package chylex.hee.system.capability
import chylex.hee.system.util.NBTBase
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage

object NullStorage : IStorage<Any>{
	@Suppress("UNCHECKED_CAST")
	fun <T> get(): IStorage<T> = this as IStorage<T>
	
	override fun writeNBT(capability: Capability<Any>, instance: Any, side: Direction?): NBTBase? = null
	override fun readNBT(capability: Capability<Any>, instance: Any, side: Direction?, nbt: NBTBase){}
}
