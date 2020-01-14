package chylex.hee.client.render.entity
import chylex.hee.client.render.entity.layer.LayerSpiderlingEyes
import chylex.hee.client.render.util.GL
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.RenderLiving
import chylex.hee.system.migration.vanilla.RenderManager
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.entity.model.SpiderModel
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityMobSpiderling(manager: RenderManager) : RenderLiving<EntityMobSpiderling, SpiderModel<EntityMobSpiderling>>(manager, SpiderModel(), 0.5F){
	private val texture = Resource.Custom("textures/entity/spiderling.png")
	
	init{
		addLayer(LayerSpiderlingEyes(this, (entityModel as SpiderModel).field_78209_a)) // RENAME spiderHead
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
