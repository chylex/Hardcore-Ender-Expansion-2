package chylex.hee.client.render.entity
import chylex.hee.client.render.entity.layer.LayerSpiderlingEyes
import chylex.hee.client.render.util.GL
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.system.Resource
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.model.ModelSpider
import net.minecraft.client.renderer.entity.RenderLiving
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityMobSpiderling(manager: RenderManager) : RenderLiving<EntityMobSpiderling>(manager, ModelSpider(), 0.5F){
	private val texture = Resource.Custom("textures/entity/spiderling.png")
	
	init{
		addLayer(LayerSpiderlingEyes(this, (mainModel as ModelSpider).spiderHead))
	}
	
	override fun preRenderCallback(entity: EntityMobSpiderling, partialTicks: Float){
		GL.scale(0.5F, 0.5F, 0.5F)
		super.preRenderCallback(entity, partialTicks)
	}
	
	override fun getEntityTexture(entity: EntityMobSpiderling): ResourceLocation{
		return texture
	}
	
	override fun getDeathMaxRotation(entity: EntityMobSpiderling): Float{
		return 180F
	}
}
