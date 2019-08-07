package chylex.hee.system.util
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry

fun <T> IForgeRegistryEntry<T>.useVanillaName(from: IForgeRegistryEntry<*>){
	with(Loader.instance()){
		val me = activeModContainer() ?: throw IllegalStateException("no mod container set during registration")
		// UPDATE ugly hack to remove warnings
		
		setActiveModContainer(minecraftModContainer)
		registryName = from.registryName
		setActiveModContainer(me)
	}
}

fun <T : IForgeRegistryEntry<T>> IForgeRegistry<T>.getIfExists(key: ResourceLocation): T?{
	return if (containsKey(key))
		getValue(key)
	else
		null
}
