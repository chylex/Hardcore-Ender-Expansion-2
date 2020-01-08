package chylex.hee.client.render.entity.layer
import chylex.hee.client.render.entity.RenderEntityMobSpiderling
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.util.MC
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.client.renderer.entity.model.RendererModel
import net.minecraft.client.renderer.entity.model.SpiderModel

@Sided(Side.CLIENT)
class LayerSpiderlingEyes(spiderlingRenderer: RenderEntityMobSpiderling, private val headRenderer: RendererModel) : LayerRenderer<EntityMobSpiderling, SpiderModel<EntityMobSpiderling>>(spiderlingRenderer){
	private val texture = Resource.Custom("textures/entity/spiderling_eyes.png")
	
	override fun render(entity: EntityMobSpiderling, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float, ageInTicks: Float, netHeadYaw: Float, headPitch: Float, scale: Float){
		if (entity.isSleeping){
			return
		}
		
		bindTexture(texture)
		
		GL.disableAlpha()
		GL.enableBlend()
		GL.blendFunc(SF_ONE, DF_ONE)
		GL.depthMask(!entity.isInvisible)
		
		GL.color(1F, 1F, 1F, 1F)
		GL.setLightmapCoords(61680F, 0F)
		MC.gameRenderer.setupFogColor(true)
		
		if (headPitch == 0F){
			GL.pushMatrix()
			GL.scale(1.001, 1.001, 1.001) // hack around z-fighting
			headRenderer.render(scale)
			GL.popMatrix()
		}
		else{
			headRenderer.render(scale)
		}
		
		MC.gameRenderer.setupFogColor(false)
		func_215334_a(entity) // UPDATE resets lightmap
		
		GL.depthMask(true)
		GL.disableBlend()
		GL.enableAlpha()
	}
	
	override fun shouldCombineTextures(): Boolean{
		return false
	}
}
