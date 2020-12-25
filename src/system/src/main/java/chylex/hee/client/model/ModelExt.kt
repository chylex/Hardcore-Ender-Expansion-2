package chylex.hee.client.model

import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.ModelRenderer
import net.minecraft.client.renderer.model.ModelRenderer.ModelBox
import net.minecraft.util.Direction
import net.minecraftforge.client.model.data.EmptyModelData
import org.apache.commons.lang3.ArrayUtils
import java.util.Random
import kotlin.concurrent.getOrSet

val ModelRenderer.beginBox
	get() = ModelBoxBuilder(this)

const val FACE_FRONT = 4
const val FACE_BACK = 5

@Sided(Side.CLIENT)
fun ModelBox.retainFace(face: Int) {
	quads = arrayOf(quads[face])
}

@Sided(Side.CLIENT)
fun ModelBox.removeFace(face: Int) {
	quads = ArrayUtils.remove(quads, face)
}

private val seedRand = ThreadLocal<Random>()

@Sided(Side.CLIENT)
fun IBakedModel.getQuads(facing: Direction? = null): MutableList<BakedQuad> {
	return this.getQuads(null, facing, seedRand.getOrSet(::Random).apply { setSeed(42L) }, EmptyModelData.INSTANCE)
}
