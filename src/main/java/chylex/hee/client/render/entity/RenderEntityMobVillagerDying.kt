package chylex.hee.client.render.entity

import chylex.hee.client.MC
import chylex.hee.client.render.gl.scale
import chylex.hee.game.entity.living.EntityMobVillagerDying
import chylex.hee.game.world.totalTime
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer
import net.minecraft.client.renderer.entity.layers.HeadLayer
import net.minecraft.client.renderer.entity.layers.VillagerLevelPendantLayer
import net.minecraft.client.renderer.entity.model.VillagerModel
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.IReloadableResourceManager
import net.minecraft.util.ResourceLocation
import java.util.Random
import kotlin.math.min

@Sided(Side.CLIENT)
class RenderEntityMobVillagerDying(manager: EntityRendererManager) : MobRenderer<EntityMobVillagerDying, VillagerModel<EntityMobVillagerDying>>(manager, Model, 0.5F) {
	private object Model : VillagerModel<EntityMobVillagerDying>(0F) {
		private val overrideOverlay = OverlayTexture.getPackedUV(OverlayTexture.getU(0F), OverlayTexture.getV(false)) // disable red hurt overlay
		
		override fun render(matrix: MatrixStack, builder: IVertexBuilder, combinedLight: Int, combinedOverlay: Int, red: Float, green: Float, blue: Float, alpha: Float) {
			super.render(matrix, builder, combinedLight, overrideOverlay, red, green, blue, alpha)
		}
	}
	
	private val rand = Random()
	private val texture = Resource.Vanilla("textures/entity/villager/villager.png")
	
	init {
		addLayer(HeadLayer(this))
		addLayer(VillagerLevelPendantLayer(this, MC.instance.resourceManager as IReloadableResourceManager, "villager"))
		addLayer(CrossedArmsItemLayer(this))
	}
	
	override fun render(entity: EntityMobVillagerDying, yaw: Float, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int) {
		rand.setSeed(entity.world.totalTime)
		val mp = min(1F, entity.deathTime / 50F) * 0.005F
		
		matrix.push()
		matrix.translate(rand.nextGaussian() * mp, rand.nextGaussian() * mp, rand.nextGaussian() * mp)
		super.render(entity, yaw, partialTicks, matrix, buffer, combinedLight)
		matrix.pop()
	}
	
	override fun getEntityTexture(entity: EntityMobVillagerDying): ResourceLocation {
		return texture
	}
	
	override fun preRenderCallback(entity: EntityMobVillagerDying, matrix: MatrixStack, partialTicks: Float) {
		val scale: Float
		
		if (entity.isChild) {
			scale = 0.46875F
			shadowSize = 0.25F
		}
		else {
			scale = 0.9375F
			shadowSize = 0.5F
		}
		
		matrix.scale(scale)
	}
	
	override fun getDeathMaxRotation(entity: EntityMobVillagerDying): Float {
		return 0F
	}
}
