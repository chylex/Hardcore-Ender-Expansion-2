package chylex.hee.client.model.entity
import chylex.hee.client.render.util.beginBox
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.entity.model.EntityModel
import net.minecraft.client.renderer.model.ModelRenderer

@Sided(Side.CLIENT)
object ModelEntityTokenHolder : EntityModel<EntityTokenHolder>(){
	private val box: ModelRenderer
	
	init{
		textureWidth = 64
		textureHeight = 32
		
		box = ModelRenderer(this).apply {
			beginBox.offset(-8F, -8F, -8F).size(16, 16, 16).add()
		}
	}
	
	override fun setRotationAngles(entity: EntityTokenHolder, limbSwing: Float, limbSwingAmount: Float, age: Float, headYaw: Float, headPitch: Float){}
	
	override fun render(matrix: MatrixStack, builder: IVertexBuilder, combinedLight: Int, combinedOverlay: Int, red: Float, green: Float, blue: Float, alpha: Float){
		box.render(matrix, builder, combinedLight, combinedOverlay, red, green, blue, alpha)
	}
}
