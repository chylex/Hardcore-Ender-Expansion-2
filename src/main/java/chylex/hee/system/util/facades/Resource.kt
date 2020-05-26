package chylex.hee.system.util.facades
import chylex.hee.HEE
import net.minecraft.util.ResourceLocation

object Resource{
	const val NAMESPACE_VANILLA = "minecraft"
	
	fun Vanilla(path: String) = ResourceLocation(NAMESPACE_VANILLA, path)
	fun Custom(path: String)  = ResourceLocation(HEE.ID, path)
	
	fun isVanilla(location: ResourceLocation) = location.namespace == NAMESPACE_VANILLA
	fun isCustom(location: ResourceLocation)  = location.namespace == HEE.ID
}
