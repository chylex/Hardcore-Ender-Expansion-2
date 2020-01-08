package chylex.hee.client.render.util
import net.minecraft.client.renderer.entity.model.RendererModel

val RendererModel.beginBox
	get() = ModelBoxBuilder(this)

fun RendererModel.render(){
	this.render(1F / 16F)
}
