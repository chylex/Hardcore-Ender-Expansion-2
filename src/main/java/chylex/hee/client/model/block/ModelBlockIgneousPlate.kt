package chylex.hee.client.model.block

import chylex.hee.client.model.util.beginBox
import chylex.hee.client.render.util.translateZ
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.model.Model
import net.minecraft.client.renderer.model.ModelRenderer
import net.minecraft.util.math.vector.Vector3d
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Sided(Side.CLIENT)
object ModelBlockIgneousPlate : Model(RenderType::getEntityCutout) {
	const val ANIMATION_PERIOD = PI
	
	private val outerBox: ModelRenderer
	private val innerBox: ModelRenderer
	
	init {
		textureWidth = 32
		textureHeight = 16
		
		outerBox = ModelRenderer(this).apply {
			beginBox.offset(12F,  4F, 0F).size( 2, 8, 2).tex(0, 6).add()
			beginBox.offset( 2F,  4F, 0F).size( 2, 8, 2).tex(8, 6).add()
			beginBox.offset( 2F,  2F, 0F).size(12, 2, 2).tex(0, 0).add()
			beginBox.offset( 2F, 12F, 0F).size(12, 2, 2).tex(0, 4).add()
		}
		
		innerBox = ModelRenderer(this).apply {
			beginBox.offset(4F, 4F, 0.5F).size(8, 8, 1).tex(14, 7).add()
		}
	}
	
	override fun render(matrix: MatrixStack, builder: IVertexBuilder, combinedLight: Int, combinedOverlay: Int, red: Float, green: Float, blue: Float, alpha: Float) {
		outerBox.render(matrix, builder, combinedLight, combinedOverlay, red, green, blue, alpha)
	}
	
	fun renderInnerBox(matrix: MatrixStack, builder: IVertexBuilder, combinedLight: Int, combinedOverlay: Int, color: Vector3d, animation: Double) {
		matrix.push()
		matrix.translateZ(-abs(sin(-animation)).toFloat() * 0.0925)
		innerBox.render(matrix, builder, combinedLight, combinedOverlay, color.x.toFloat(), color.y.toFloat(), color.z.toFloat(), 1F)
		matrix.pop()
	}
}
