package chylex.hee.client.model.entity
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class ModelEntityTokenHolder : ModelBase(){
	private val box = ModelRenderer(this, 0, 0)
	
	init{
		textureWidth = 64
		textureHeight = 32
		
		box.addBox(-8F, -8F, -8F, 16, 16, 16)
	}
	
	fun render(){
		box.render(0.0625F)
	}
}
