package chylex.hee.system
import chylex.hee.HEE
import net.minecraft.util.ResourceLocation

object Resource{
	inline fun Vanilla(path: String): ResourceLocation{
		return ResourceLocation("minecraft", path)
	}
	
	inline fun Custom(path: String): ResourceLocation{
		return ResourceLocation(HEE.ID, path)
	}
}
