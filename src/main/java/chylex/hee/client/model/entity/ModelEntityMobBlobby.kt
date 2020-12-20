package chylex.hee.client.model.entity
import chylex.hee.client.MC
import chylex.hee.client.model.FACE_FRONT
import chylex.hee.client.model.beginBox
import chylex.hee.client.model.removeFace
import chylex.hee.client.model.retainFace
import chylex.hee.client.render.gl.scale
import chylex.hee.client.render.gl.translateY
import chylex.hee.game.entity.living.EntityMobBlobby
import chylex.hee.game.entity.posVec
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.Vec3
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.entity.model.EntityModel
import net.minecraft.client.renderer.model.ModelRenderer

@Sided(Side.CLIENT)
object ModelEntityMobBlobby : EntityModel<EntityMobBlobby>(){
	const val GLOBAL_SCALE = 8F / 9F
	
	private const val SIZE_BODY = 9
	private const val FACE_SCALE = 8
	private const val SIZE_FACE = SIZE_BODY * FACE_SCALE
	
	private val body: ModelRenderer
	private val faceFront: ModelRenderer
	private val faceBack: ModelRenderer
	
	private var isPlayerInFront = false
	
	private var r = 255F
	private var g = 255F
	private var b = 55F
	
	init{
		textureWidth = 128
		textureHeight = 256
		
		body = ModelRenderer(this).apply {
			beginBox.offset(-SIZE_BODY * 0.5F, 15F, -SIZE_BODY * 0.5F).size(SIZE_BODY, SIZE_BODY, SIZE_BODY).tex(0, 0).add()
			cubeList[0].removeFace(FACE_FRONT)
		}
		
		faceFront = ModelRenderer(this).apply {
			beginBox.offset(-SIZE_FACE * 0.5F, 15F * FACE_SCALE, -SIZE_FACE * 0.5F).size(SIZE_FACE, SIZE_FACE, SIZE_FACE).tex(-SIZE_FACE, 19 - SIZE_FACE).add()
			cubeList[0].retainFace(FACE_FRONT)
		}
		
		faceBack = ModelRenderer(this).apply {
			beginBox.offset(-SIZE_FACE * 0.5F, 15F * FACE_SCALE, -SIZE_FACE * 0.5F).size(SIZE_FACE, SIZE_FACE, SIZE_FACE).tex(-SIZE_FACE, 92 - SIZE_FACE).add()
			cubeList[0].retainFace(FACE_FRONT)
		}
	}
	
	override fun setLivingAnimations(entity: EntityMobBlobby, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float){
		entity.color.let {
			r = it.redF
			g = it.greenF
			b = it.blueF
		}
	}
	
	override fun setRotationAngles(entity: EntityMobBlobby, limbSwing: Float, limbSwingAmount: Float, age: Float, headYaw: Float, headPitch: Float){
		val vecBlobbyLook = Vec3.fromYaw(entity.rotationYawHead)
		val vecCameraDiff = MC.renderManager.info.projectedView.subtract(entity.posVec).normalize()
		
		isPlayerInFront = vecBlobbyLook.dotProduct(vecCameraDiff) > 0.0
	}
	
	override fun render(matrix: MatrixStack, builder: IVertexBuilder, combinedLight: Int, combinedOverlay: Int, red: Float, green: Float, blue: Float, alpha: Float){
		matrix.translateY(-0.001)
		
		matrix.push()
		matrix.scale(1F / FACE_SCALE)
		
		(if (isPlayerInFront) faceFront else faceBack).render(matrix, builder, combinedLight, combinedOverlay, r, g, b, 1F)
		
		matrix.pop()
		
		body.render(matrix, builder, combinedLight, combinedOverlay, r, g, b, 1F)
	}
}
