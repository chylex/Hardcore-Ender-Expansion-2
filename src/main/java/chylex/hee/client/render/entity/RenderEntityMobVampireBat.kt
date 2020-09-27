package chylex.hee.client.render.entity
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityBat
import chylex.hee.system.migration.RenderBat
import chylex.hee.system.migration.RenderManager
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityMobVampireBat(manager: RenderManager) : RenderBat(manager){
	private val texture = Resource.Custom("textures/entity/vampire_bat.png")
	
	override fun getEntityTexture(entity: EntityBat): ResourceLocation{
		return texture
	}
}
