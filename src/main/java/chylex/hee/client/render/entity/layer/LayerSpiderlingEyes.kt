package chylex.hee.client.render.entity.layer
import chylex.hee.client.render.entity.RenderEntityMobSpiderling
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.util.MC
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.system.Resource
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class LayerSpiderlingEyes(private val spiderlingRenderer: RenderEntityMobSpiderling, private val headRenderer: ModelRenderer) : LayerRenderer<EntityMobSpiderling>{
	private val texture = Resource.Custom("textures/entity/spiderling_eyes.png")
	
	override fun doRenderLayer(entity: EntityMobSpiderling, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float, ageInTicks: Float, netHeadYaw: Float, headPitch: Float, scale: Float){
		if (entity.isSleeping){
			return
		}
		
		spiderlingRenderer.bindTexture(texture)
		
		GL.color(1F, 1F, 1F, 1F)
		GL.disableAlpha()
		GL.enableBlend()
		GL.blendFunc(SF_ONE, DF_ONE)
		GL.depthMask(!entity.isInvisible)
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680F, 0F)
		MC.entityRenderer.setupFogColor(true)
		
		if (headPitch == 0F){
			GL.pushMatrix()
			GL.scale(1.001, 1.001, 1.001) // hack around z-fighting
			headRenderer.render(scale)
			GL.popMatrix()
		}
		else{
			headRenderer.render(scale)
		}
		
		spiderlingRenderer.setLightmap(entity)
		MC.entityRenderer.setupFogColor(false)
		
		GL.disableBlend()
		GL.enableAlpha()
	}
	
	override fun shouldCombineTextures(): Boolean{
		return false
	}
}
