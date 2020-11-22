package chylex.hee.client.render.entity
import chylex.hee.client.render.gl.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.gl.RenderStateBuilder
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.CULL_DISABLED
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.LIGHTING_ENABLED
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.LIGHTMAP_ENABLED
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.MASK_COLOR
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.OVERLAY_DISABLED
import chylex.hee.client.render.gl.SF_SRC_ALPHA
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.world.totalTime
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityEnderman
import chylex.hee.system.random.nextFloat
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EndermanRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.client.renderer.entity.model.EndermanModel
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.util.Random

@Sided(Side.CLIENT)
open class RenderEntityMobAbstractEnderman(manager: EntityRendererManager) : EndermanRenderer(manager){
	private fun RENDER_TYPE_CLONE(tex: ResourceLocation) = with(RenderStateBuilder()){
		tex(tex)
		blend(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA)
		lighting(LIGHTING_ENABLED)
		alpha(0.004F)
		cull(CULL_DISABLED)
		lightmap(LIGHTMAP_ENABLED)
		overlay(OVERLAY_DISABLED)
		mask(MASK_COLOR)
		buildType("hee:enderman_clone", DefaultVertexFormats.ENTITY, GL11.GL_QUADS, bufferSize = 256, useDelegate = true)
	}
	
	private val rand = Random()
	private val originalLayerList: List<LayerRenderer<EntityEnderman, EndermanModel<EntityEnderman>>>
	private var isRenderingClone = false
	
	init{
		entityModel = object : EndermanModel<EntityEnderman>(0F){
			override fun render(matrix: MatrixStack, builder: IVertexBuilder, combinedLight: Int, combinedOverlay: Int, red: Float, green: Float, blue: Float, alpha: Float){
				super.render(matrix, builder, combinedLight, combinedOverlay, red, green, blue, if (isRenderingClone) rand.nextFloat(0.05F, 0.3F) else alpha)
			}
		}
		
		originalLayerList = ArrayList(layerRenderers)
	}
	
	override fun render(entity: EntityEnderman, yaw: Float, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int){
		if (entity !is EntityMobAbstractEnderman){
			return
		}
		
		if (entity.isShaking){
			rand.setSeed(entity.world.totalTime)
			
			matrix.push()
			matrix.translate(rand.nextGaussian() * 0.01, rand.nextGaussian() * 0.01, rand.nextGaussian() * 0.01)
			super.render(entity, yaw, partialTicks, matrix, buffer, combinedLight)
			matrix.pop()
		}
		else{
			super.render(entity, yaw, partialTicks, matrix, buffer, combinedLight)
		}
		
		val cloneCount = getCloneCount(entity)
		
		if (cloneCount > 0){
			rand.setSeed(entity.world.totalTime * 2L / 3L)
			
			val prevPrevYaw = entity.prevRotationYawHead
			val prevYaw = entity.rotationYawHead
			
			val prevPrevPitch = entity.prevRotationPitch
			val prevPitch = entity.rotationPitch
			
			isRenderingClone = true
			layerRenderers.clear()
			
			repeat(cloneCount){
				if (rand.nextInt(3) == 0){
					entity.rotationYawHead += rand.nextFloat(-45F, 45F)
					entity.prevRotationYawHead = entity.rotationYawHead
					
					entity.rotationPitch += rand.nextFloat(-30F, 30F)
					entity.prevRotationPitch = entity.rotationPitch
				}
				
				matrix.push()
				matrix.translate(rand.nextGaussian() * 0.04, rand.nextGaussian() * 0.025, rand.nextGaussian() * 0.04)
				super.render(entity, yaw, partialTicks, matrix, buffer, combinedLight)
				matrix.pop()
			}
			
			entity.prevRotationYawHead = prevPrevYaw
			entity.rotationYawHead = prevYaw
			
			entity.prevRotationPitch = prevPrevPitch
			entity.rotationPitch = prevPitch
			
			layerRenderers.addAll(originalLayerList)
			isRenderingClone = false
		}
	}
	
	protected open fun getCloneCount(entity: EntityMobAbstractEnderman): Int{
		return if (entity.hurtTime == 0 && entity.isAggro) 2 else 0
	}
	
	override fun func_230496_a_(entity: EntityEnderman, isVisible: Boolean, isTranslucent: Boolean, isGlowing: Boolean): RenderType?{
		return if (isRenderingClone)
			RENDER_TYPE_CLONE(getEntityTexture(entity))
		else
			super.func_230496_a_(entity, isVisible, isTranslucent, isGlowing)
	}
}
