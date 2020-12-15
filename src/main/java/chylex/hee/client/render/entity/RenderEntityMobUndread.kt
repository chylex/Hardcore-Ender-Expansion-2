package chylex.hee.client.render.entity
import chylex.hee.client.model.entity.ModelEntityMobUndread
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.entity.BipedRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer
import net.minecraft.client.renderer.entity.model.AbstractZombieModel
import net.minecraft.util.ResourceLocation

@Sided(Side.CLIENT)
class RenderEntityMobUndread(manager: EntityRendererManager) : BipedRenderer<EntityMobUndread, AbstractZombieModel<EntityMobUndread>>(manager, ModelEntityMobUndread(), 0.5F){
	private val texture = Resource.Custom("textures/entity/undread.png")
	
	init{
		addLayer(BipedArmorLayer(this, ModelEntityMobUndread(0.5125F, true), ModelEntityMobUndread(1F, true)))
	}
	
	override fun preRenderCallback(entity: EntityMobUndread, matrix: MatrixStack, partialTicks: Float){
		matrix.scale(1.025F, 0.965F, 1.025F)
		super.preRenderCallback(entity, matrix, partialTicks)
	}
	
	override fun getEntityTexture(entity: EntityMobUndread): ResourceLocation{
		return texture
	}
	
	override fun getDeathMaxRotation(entity: EntityMobUndread): Float{
		val uuid = entity.uniqueID
		return 15F * (if ((uuid.leastSignificantBits % 2L) xor (uuid.mostSignificantBits % 2L) == 0L) 1F else -1F)
	}
}
