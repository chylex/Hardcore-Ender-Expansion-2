package chylex.hee.client.render.entity
import chylex.hee.client.model.entity.ModelEntityTokenHolder
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.item.ItemPortalToken.TokenType.NORMAL
import chylex.hee.game.item.ItemPortalToken.TokenType.RARE
import chylex.hee.game.item.ItemPortalToken.TokenType.SOLITARY
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation
import kotlin.math.pow

@Sided(Side.CLIENT)
class RenderEntityTokenHolder(manager: RenderManager) : Render<EntityTokenHolder>(manager){
	private val textures = mapOf(
		NORMAL   to Resource.Custom("textures/entity/token_holder.png"),
		RARE     to Resource.Custom("textures/entity/token_holder_rare.png"),
		SOLITARY to Resource.Custom("textures/entity/token_holder_solitary.png")
	)
	
	override fun doRender(entity: EntityTokenHolder, x: Double, y: Double, z: Double, rotationYaw: Float, partialTicks: Float){
		val charge = entity.renderCharge.get(partialTicks)
		val scale = 0.25F + (0.25F * charge.pow(1.5F))
		val alpha = 0.35F + (0.475F * charge.pow(5.5F))
		
		GL.pushMatrix()
		GL.translate(x, y + (entity.height * 0.5F), z)
		
		GL.pushMatrix()
		GL.scale(scale, scale, scale)
		GL.rotate(entity.renderRotation.get(partialTicks), 0F, 1F, 0F)
		GL.rotate(55F, 1F, 0F, 1F)
		
		GL.enableBlend()
		GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA)
		GL.color(1F, 1F, 1F, alpha)
		
		bindEntityTexture(entity)
		ModelEntityTokenHolder.render()
		
		GL.popMatrix()
		GL.disableBlend()
		GL.popMatrix()
	}
	
	override fun getEntityTexture(entity: EntityTokenHolder): ResourceLocation?{
		return textures[entity.tokenType]
	}
}
