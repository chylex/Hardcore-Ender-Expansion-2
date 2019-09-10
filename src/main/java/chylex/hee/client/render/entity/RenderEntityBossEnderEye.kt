package chylex.hee.client.render.entity
import chylex.hee.client.model.entity.ModelEntityBossEnderEye
import chylex.hee.client.model.entity.ModelEntityBossEnderEye.SCALE
import chylex.hee.client.render.util.GL
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.system.Resource
import net.minecraft.client.renderer.entity.RenderLiving
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class RenderEntityBossEnderEye(manager: RenderManager) : RenderLiving<EntityBossEnderEye>(manager, ModelEntityBossEnderEye, SCALE){
	private val texture = Resource.Custom("textures/entity/ender_eye.png")
	
	override fun preRenderCallback(entity: EntityBossEnderEye, partialTicks: Float){
		GL.scale(SCALE, SCALE, SCALE)
		super.preRenderCallback(entity, partialTicks)
	}
	
	override fun getEntityTexture(entity: EntityBossEnderEye): ResourceLocation{
		return texture
	}
}
