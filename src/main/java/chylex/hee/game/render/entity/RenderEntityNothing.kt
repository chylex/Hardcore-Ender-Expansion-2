package chylex.hee.game.render.entity
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class RenderEntityNothing(manager: RenderManager) : Render<Entity>(manager){
	override fun shouldRender(entity: Entity, camera: ICamera, camX: Double, camY: Double, camZ: Double) = false
	override fun getEntityTexture(entity: Entity): ResourceLocation? = null
}
