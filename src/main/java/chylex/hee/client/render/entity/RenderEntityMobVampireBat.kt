package chylex.hee.client.render.entity
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.entity.RenderBat
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.entity.passive.EntityBat
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityMobVampireBat(manager: RenderManager) : RenderBat(manager){
	private val texture = Resource.Custom("textures/entity/vampire_bat.png")
	
	override fun getEntityTexture(entity: EntityBat): ResourceLocation{
		return texture
	}
}
