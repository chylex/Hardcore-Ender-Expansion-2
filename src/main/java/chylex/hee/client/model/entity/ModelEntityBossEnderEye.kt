package chylex.hee.client.model.entity

import chylex.hee.client.model.util.FACE_FRONT
import chylex.hee.client.model.util.beginBox
import chylex.hee.client.model.util.retainFace
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.toRadians
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.entity.model.EntityModel
import net.minecraft.client.renderer.model.ModelRenderer

@Sided(Side.CLIENT)
object ModelEntityBossEnderEye : EntityModel<EntityBossEnderEye>() {
	const val SCALE = 16F / 18F
	
	private val head: ModelRenderer
	private val eyes: Array<ModelRenderer>
	private val arms: ModelRenderer
	
	private var eyeState = 0
	
	init {
		textureWidth = 128
		textureHeight = 64
		
		head = ModelRenderer(this).apply {
			setRotationPoint(0F, 15F, 0F)
			beginBox.offset(-9F, -9F, -9F).size(18, 18, 18).tex(0, 0).add()
		}
		
		eyes = Array(8) {
			ModelRenderer(this).apply {
				setRotationPoint(0F, 15F, 0F)
				beginBox.offset(-8F, -8F, -9F).size(16, 16, 1).tex(-1 + (16 * it), 47).add()
				cubeList[0].retainFace(FACE_FRONT)
			}
		}
		
		arms = ModelRenderer(this).apply {
			setRotationPoint(0F, 15.5F, -0.5F)
			beginBox.offset(-12F, -1.5F, -1.5F).size(3, 27, 3).tex(73, 0).add()
			beginBox.offset(  9F, -1.5F, -1.5F).size(3, 27, 3).tex(73, 0).mirror().add()
		}
	}
	
	override fun setLivingAnimations(entity: EntityBossEnderEye, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float) {
		arms.rotateAngleX = entity.clientArmAngle.get(partialTicks).toRadians()
		
		eyeState = when {
			entity.eyeState == EntityBossEnderEye.EYE_CLOSED -> 0
			entity.eyeState == EntityBossEnderEye.EYE_LASER  -> 7
			entity.isDemonEye                                -> 7
			else                                             -> entity.demonLevel + 1
		}
	}
	
	override fun setRotationAngles(entity: EntityBossEnderEye, limbSwing: Float, limbSwingAmount: Float, age: Float, headYaw: Float, headPitch: Float) {
		head.rotateAngleX = headPitch.toRadians()
	}
	
	override fun render(matrix: MatrixStack, builder: IVertexBuilder, combinedLight: Int, combinedOverlay: Int, red: Float, green: Float, blue: Float, alpha: Float) {
		head.render(matrix, builder, combinedLight, combinedOverlay, red, green, blue, alpha)
		eyes[eyeState].also { it.rotateAngleX = head.rotateAngleX }.render(matrix, builder, combinedLight, combinedOverlay, red, green, blue, alpha)
		arms.render(matrix, builder, combinedLight, combinedOverlay, red, green, blue, alpha)
	}
}
