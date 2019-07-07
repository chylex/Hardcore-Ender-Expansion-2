package chylex.hee.client.render.entity
import chylex.hee.client.render.util.GL
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.system.Resource
import net.minecraft.client.model.ModelZombie
import net.minecraft.client.renderer.entity.RenderBiped
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class RenderEntityMobUndread(manager: RenderManager) : RenderBiped<EntityMobUndread>(manager, ModelZombie(), 0.5F){
	private val texture = Resource.Custom("textures/entity/undread.png")
	
	init{
		addLayer(object : LayerBipedArmor(this){
			override fun initArmor(){
				modelLeggings = ModelZombie(0.5125F, true)
				modelArmor = ModelZombie(1F, true)
			}
		})
	}
	
	override fun preRenderCallback(entity: EntityMobUndread, partialTicks: Float){
		GL.scale(1.025F, 0.965F, 1.025F)
		super.preRenderCallback(entity, partialTicks)
	}
	
	override fun getEntityTexture(entity: EntityMobUndread): ResourceLocation{
		return texture
	}
	
	override fun getDeathMaxRotation(entity: EntityMobUndread): Float{
		val uuid = entity.uniqueID
		return 15F * (if ((uuid.leastSignificantBits % 2L) xor (uuid.mostSignificantBits % 2L) == 0L) 1F else -1F)
	}
}
