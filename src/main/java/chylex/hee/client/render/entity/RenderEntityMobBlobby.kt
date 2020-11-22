package chylex.hee.client.render.entity
import chylex.hee.client.MC
import chylex.hee.client.model.ModelHelper
import chylex.hee.client.model.entity.ModelEntityMobBlobby
import chylex.hee.client.model.entity.ModelEntityMobBlobby.GLOBAL_SCALE
import chylex.hee.client.model.getQuads
import chylex.hee.client.render.gl.rotateX
import chylex.hee.client.render.gl.rotateY
import chylex.hee.client.render.gl.rotateZ
import chylex.hee.client.render.gl.scale
import chylex.hee.client.render.gl.scaleY
import chylex.hee.client.render.gl.translateY
import chylex.hee.game.entity.living.EntityMobBlobby
import chylex.hee.game.inventory.isNotEmpty
import chylex.hee.system.facades.Facing6
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.ItemBlock
import chylex.hee.system.random.nextFloat
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.block.AbstractChestBlock
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.Atlases
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GROUND
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.ForgeHooksClient
import java.util.Random

@Sided(Side.CLIENT)
class RenderEntityMobBlobby(manager: EntityRendererManager) : MobRenderer<EntityMobBlobby, ModelEntityMobBlobby>(manager, ModelEntityMobBlobby, 0.27F){
	private val texture = Resource.Custom("textures/entity/blobby.png")
	private val renderType = RenderType.getEntityTranslucent(texture)
	private val fallbackStack = ItemStack(Blocks.BEDROCK)
	private val rand = Random()
	
	init{
		shadowOpaque = 0.6F
	}
	
	override fun preRenderCallback(entity: EntityMobBlobby, matrix: MatrixStack, partialTicks: Float){
		matrix.scale(entity.scale * GLOBAL_SCALE)
		matrix.scaleY(1F + entity.renderSquishClient.get(partialTicks))
		
		super.preRenderCallback(entity, matrix, partialTicks)
	}
	
	override fun render(entity: EntityMobBlobby, yaw: Float, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int){
		val scale = entity.scale
		val stack = entity.heldItem
		
		if (stack.isNotEmpty && entity.deathTime == 0){
			renderItemInGel(stack, entity, matrix, buffer, combinedLight)
		}
		
		shadowSize = 0.27F * scale
		super.render(entity, yaw, partialTicks, matrix, buffer, combinedLight)
	}
	
	override fun func_230496_a_(entity: EntityMobBlobby, isVisible: Boolean, isTranslucent: Boolean, isGlowing: Boolean): RenderType{
		return renderType
	}
	
	override fun getEntityTexture(entity: EntityMobBlobby): ResourceLocation{
		return texture
	}
	
	private fun renderItemInGel(stack: ItemStack, entity: EntityMobBlobby, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int){
		val scale = entity.scale
		
		matrix.push()
		val model = ForgeHooksClient.handleCameraTransforms(matrix, ModelHelper.getItemModel(stack), GROUND, false)
		
		val modelYOff: Double
		val modelRotOff: Double
		val modelScale: Float
		
		if (model.isGui3d){
			modelYOff = 0.75
			modelRotOff = 0.0
			modelScale = 0.75F
		}
		else{
			modelYOff = 0.5
			modelRotOff = 0.1
			modelScale = 0.66F
		}
		
		rand.setSeed(entity.uniqueID.leastSignificantBits)
		matrix.translateY(modelRotOff)
		matrix.rotateX(rand.nextFloat(0F, 360F))
		matrix.rotateY(rand.nextFloat(0F, 360F))
		matrix.rotateZ(rand.nextFloat(0F, 360F))
		matrix.translateY(-modelRotOff)
		
		matrix.scale(modelScale * scale)
		matrix.translate(-0.5, entity.height * scale * 0.5 - modelYOff, -0.5)
		
		if (model.isBuiltInRenderer){
			val overrideType = when((stack.item as? ItemBlock)?.block){
				is AbstractChestBlock<*> -> RenderType.getEntityTranslucentCull(Atlases.CHEST_ATLAS)
				else -> null // POLISH implement more special cases
			}
			
			if (overrideType != null){ // UPDATE test transform
				stack.item.itemStackTileEntityRenderer.func_239207_a_(stack, TransformType.NONE, matrix, { buffer.getBuffer(overrideType) }, combinedLight, OverlayTexture.NO_OVERLAY)
			}
			else if (stack !== fallbackStack){
				matrix.pop()
				renderItemInGel(fallbackStack, entity, matrix, buffer, combinedLight)
				return
			}
		}
		else{
			val builder = buffer.getBuffer(Atlases.getTranslucentCullBlockType())
			
			for(facing in Facing6){
				renderItemQuads(stack, model, facing, matrix, builder, combinedLight)
			}
			
			renderItemQuads(stack, model, null, matrix, builder, combinedLight)
		}
		
		matrix.pop()
	}
	
	private fun renderItemQuads(stack: ItemStack, model: IBakedModel, facing: Direction?, matrix: MatrixStack, builder: IVertexBuilder, combinedLight: Int){
		MC.itemRenderer.renderQuads(matrix, builder, model.getQuads(facing), stack, combinedLight, OverlayTexture.NO_OVERLAY)
	}
}
