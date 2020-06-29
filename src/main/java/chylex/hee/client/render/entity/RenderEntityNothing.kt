package chylex.hee.client.render.entity
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.Render
import chylex.hee.system.migration.vanilla.RenderManager
import net.minecraft.client.renderer.culling.ClippingHelperImpl
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityNothing(manager: RenderManager) : Render<Entity>(manager){
	override fun shouldRender(entity: Entity, camera: ClippingHelperImpl, camX: Double, camY: Double, camZ: Double) = false
	override fun getEntityTexture(entity: Entity): ResourceLocation? = null
}
