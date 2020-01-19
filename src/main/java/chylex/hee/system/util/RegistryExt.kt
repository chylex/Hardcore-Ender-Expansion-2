package chylex.hee.system.util
import chylex.hee.HEE
import chylex.hee.system.util.facades.Resource
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry

fun <T> IForgeRegistryEntry<T>.useVanillaName(from: IForgeRegistryEntry<*>){
	val name = from.registryName!!
	HEE.log.info("Overriding vanilla ${this.registryType.simpleName}: ${name.path}")
	
	with(ModLoadingContext.get()){
		val me = activeContainer ?: throw IllegalStateException("no mod container set during registration")
		val ext = extension<Any>()
		
		// UPDATE ugly hack to remove warnings
		setActiveContainer(ModList.get().getModContainerById("minecraft").get(), null)
		registryName = name
		setActiveContainer(me, ext)
	}
}

fun <T : IForgeRegistryEntry<T>> IForgeRegistry<T>.getIfExists(key: ResourceLocation): T?{
	return if (containsKey(key))
		getValue(key)
	else
		null
}

infix fun <T : IForgeRegistryEntry<*>> T.named(registryName: String) = apply {
	this.registryName = Resource.Custom(registryName)
}
