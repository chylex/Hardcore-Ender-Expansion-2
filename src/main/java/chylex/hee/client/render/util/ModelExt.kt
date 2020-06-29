package chylex.hee.client.render.util
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.renderer.model.ModelRenderer
import net.minecraft.client.renderer.model.ModelRenderer.ModelBox

val ModelRenderer.beginBox
	get() = ModelBoxBuilder(this)

const val FACE_FRONT = 4
const val FACE_BACK = 5

@Sided(Side.CLIENT)
fun ModelBox.retainFace(face: Int){
	quads = arrayOf(quads[face])
}
