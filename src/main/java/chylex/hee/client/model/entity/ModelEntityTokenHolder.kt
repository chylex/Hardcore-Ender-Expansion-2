package chylex.hee.client.model.entity
import chylex.hee.client.render.util.beginBox
import chylex.hee.client.render.util.render
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer

@Sided(Side.CLIENT)
object ModelEntityTokenHolder : ModelBase(){
	private val box: ModelRenderer
	
	init{
		textureWidth = 64
		textureHeight = 32
		
		box = ModelRenderer(this).apply {
			beginBox.offset(-8F, -8F, -8F).size(16, 16, 16).add()
		}
	}
	
	fun render(){
		box.render()
	}
}
