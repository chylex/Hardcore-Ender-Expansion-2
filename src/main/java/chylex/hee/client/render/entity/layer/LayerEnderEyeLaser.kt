package chylex.hee.client.render.entity.layer
import chylex.hee.client.model.entity.ModelEntityBossEnderEye
import chylex.hee.client.render.gl.RenderStateBuilder
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.CULL_DISABLED
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.LIGHTMAP_ENABLED
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.SHADE_ENABLED
import chylex.hee.client.render.gl.rotateX
import chylex.hee.client.render.gl.rotateZ
import chylex.hee.client.render.gl.translateY
import chylex.hee.client.render.gl.translateZ
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.world.totalTime
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.entity.IEntityRenderer
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.client.renderer.tileentity.BeaconTileEntityRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11

@Sided(Side.CLIENT)
class LayerEnderEyeLaser(entity: IEntityRenderer<EntityBossEnderEye, ModelEntityBossEnderEye>) : LayerRenderer<EntityBossEnderEye, ModelEntityBossEnderEye>(entity){
	private val renderType = with(RenderStateBuilder()){
		tex(BeaconTileEntityRenderer.TEXTURE_BEACON_BEAM)
		shade(SHADE_ENABLED)
		cull(CULL_DISABLED)
		lightmap(LIGHTMAP_ENABLED)
		buildType("hee:ender_eye_laser", DefaultVertexFormats.POSITION_COLOR_TEX, drawMode = GL11.GL_QUADS, bufferSize = 256)
	}
	
	override fun render(matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, entity: EntityBossEnderEye, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float, age: Float, headYaw: Float, headPitch: Float){
		if (entity.eyeState != EntityBossEnderEye.EYE_LASER){
			return
		}
		
		val builder = buffer.getBuffer(renderType)
		val rotation = Math.floorMod(entity.world.totalTime, 12L) + partialTicks
		
		matrix.push()
		matrix.translateY(0.935) // ???
		matrix.rotateX(headPitch)
		matrix.translateZ(-(entity.width * 0.5) - 0.0125)
		matrix.rotateZ(rotation * 7.5F * 1F)
		
		val hw = 0.04F
		val len = entity.getLaserLength(partialTicks)
		val tex = len * 1500F
		val mat = matrix.last.matrix
		
		builder.pos(mat, -hw, -hw,   0F).color().tex(0F, 0F).endVertex()
		builder.pos(mat, -hw, -hw, -len).color().tex(0F, tex).endVertex()
		builder.pos(mat,  hw, -hw, -len).color().tex(1F, tex).endVertex()
		builder.pos(mat,  hw, -hw,   0F).color().tex(1F, 0F).endVertex()
		
		builder.pos(mat, -hw,  hw,   0F).color().tex(0F, 0F).endVertex()
		builder.pos(mat, -hw,  hw, -len).color().tex(0F, tex).endVertex()
		builder.pos(mat, -hw, -hw, -len).color().tex(1F, tex).endVertex()
		builder.pos(mat, -hw, -hw,   0F).color().tex(1F, 0F).endVertex()
		
		builder.pos(mat, hw, -hw,   0F).color().tex(0F, 0F).endVertex()
		builder.pos(mat, hw, -hw, -len).color().tex(0F, tex).endVertex()
		builder.pos(mat, hw,  hw, -len).color().tex(1F, tex).endVertex()
		builder.pos(mat, hw,  hw,   0F).color().tex(1F, 0F).endVertex()
		
		builder.pos(mat,  hw, hw,   0F).color().tex(0F, 0F).endVertex()
		builder.pos(mat,  hw, hw, -len).color().tex(0F, tex).endVertex()
		builder.pos(mat, -hw, hw, -len).color().tex(1F, tex).endVertex()
		builder.pos(mat, -hw, hw,   0F).color().tex(1F, 0F).endVertex()
		
		builder.pos(mat, -hw, -hw, -len).color().tex(0F, 0F).endVertex()
		builder.pos(mat, -hw,  hw, -len).color().tex(0F, 0F).endVertex()
		builder.pos(mat,  hw,  hw, -len).color().tex(0F, 0F).endVertex()
		builder.pos(mat,  hw, -hw, -len).color().tex(0F, 0F).endVertex()
		
		matrix.pop()
	}
	
	private fun IVertexBuilder.color(): IVertexBuilder{
		return this.color(0.99F, 0.11F, 0.08F, 1F)
	}
}
