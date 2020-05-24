package chylex.hee.client.render.util
import net.minecraft.client.renderer.entity.model.RendererModel
import net.minecraft.client.renderer.model.ModelBox

val RendererModel.beginBox
	get() = ModelBoxBuilder(this)

fun RendererModel.render(){
	this.render(1F / 16F)
}

const val FACE_FRONT = 4
const val FACE_BACK = 5

fun ModelBox.retainFace(face: Int){
	quads = arrayOf(quads[face])
}
