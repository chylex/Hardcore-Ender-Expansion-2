package chylex.hee.client.render.util
import net.minecraft.client.model.ModelRenderer

val ModelRenderer.beginBox
	get() = ModelBoxBuilder(this)

fun ModelRenderer.render(){
	this.render(1F / 16F)
}
