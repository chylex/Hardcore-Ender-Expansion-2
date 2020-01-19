package chylex.hee.client.render.entity
import chylex.hee.client.model.entity.ModelEntityUndread
import chylex.hee.client.render.util.GL
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.RenderBiped
import chylex.hee.system.migration.vanilla.RenderManager
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer
import net.minecraft.client.renderer.entity.model.AbstractZombieModel
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityMobUndread(manager: RenderManager) : RenderBiped<EntityMobUndread, AbstractZombieModel<EntityMobUndread>>(manager, ModelEntityUndread(), 0.5F){
	private val texture = Resource.Custom("textures/entity/undread.png")
	
	init{
		addLayer(BipedArmorLayer(this, ModelEntityUndread(0.5125F, true), ModelEntityUndread(1F, true)))
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
