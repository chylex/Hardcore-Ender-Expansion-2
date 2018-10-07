package chylex.hee.system
import chylex.hee.HEE
import net.minecraft.util.ResourceLocation

sealed class Resource(private val domain: String){
	object Vanilla : Resource("minecraft")
	object Custom  : Resource(HEE.ID)
	
	operator fun invoke(path: String): ResourceLocation{
		return ResourceLocation(domain, path)
	}
}
