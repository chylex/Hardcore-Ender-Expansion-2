package chylex.hee.client.render.entity

import chylex.hee.client.render.entity.layer.LayerSpiderlingEyes
import chylex.hee.client.render.gl.scale
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.game.world.Pos
import chylex.hee.game.world.isLoaded
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.floorToInt
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.model.SpiderModel
import net.minecraft.util.ResourceLocation
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.LightType.SKY

@Sided(Side.CLIENT)
class RenderEntityMobSpiderling(manager: EntityRendererManager) : MobRenderer<EntityMobSpiderling, SpiderModel<EntityMobSpiderling>>(manager, SpiderModel(), 0.5F) {
	private val texture = Resource.Custom("textures/entity/spiderling.png")
	
	init {
		addLayer(LayerSpiderlingEyes(this, (entityModel as SpiderModel).spiderHead))
	}
	
	override fun preRenderCallback(entity: EntityMobSpiderling, matrix: MatrixStack, partialTicks: Float) {
		matrix.scale(0.5F)
		super.preRenderCallback(entity, matrix, partialTicks)
	}
	
	override fun getEntityTexture(entity: EntityMobSpiderling): ResourceLocation {
		return texture
	}
	
	override fun getPackedLight(entity: EntityMobSpiderling, partialTicks: Float): Int {
		val world = entity.world
		val pos = Pos(entity)
		
		if (!pos.isLoaded(world)) {
			return 0
		}
		
		val sky = (world.getLightFor(SKY, pos) * 0.77).floorToInt()
		val block = (world.getLightFor(BLOCK, pos) * 0.77).floorToInt()
		
		return LightTexture.packLight(sky, block)
	}
	
	override fun getDeathMaxRotation(entity: EntityMobSpiderling): Float {
		return 180F
	}
}
