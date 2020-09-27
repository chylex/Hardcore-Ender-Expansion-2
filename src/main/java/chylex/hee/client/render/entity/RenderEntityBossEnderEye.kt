package chylex.hee.client.render.entity
import chylex.hee.client.model.entity.ModelEntityBossEnderEye
import chylex.hee.client.model.entity.ModelEntityBossEnderEye.SCALE
import chylex.hee.client.render.gl.scale
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.RenderLiving
import chylex.hee.system.migration.RenderManager
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityBossEnderEye(manager: RenderManager) : RenderLiving<EntityBossEnderEye, ModelEntityBossEnderEye>(manager, ModelEntityBossEnderEye, SCALE){
	private val texture = Resource.Custom("textures/entity/ender_eye.png")
	
	override fun preRenderCallback(entity: EntityBossEnderEye, matrix: MatrixStack, partialTicks: Float){
		matrix.scale(SCALE)
		super.preRenderCallback(entity, matrix, partialTicks)
	}
	
	override fun getEntityTexture(entity: EntityBossEnderEye): ResourceLocation{
		return texture
	}
}
