package chylex.hee.client.render.block
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.renderer.entity.model.GenericHeadModel
import net.minecraft.client.renderer.entity.model.RendererModel
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack

@Sided(Side.CLIENT)
object RenderTileEndermanHead{
	private val TEX_ENDERMAN = Resource.Custom("textures/entity/enderman_head.png")
	private val MODEL_HEAD = GenericHeadModel(0, 0, 64, 32)
	
	@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
	private fun renderHead(){
		MC.textureManager.bindTexture(TEX_ENDERMAN)
		MODEL_HEAD.func_217104_a(0F, 0F, 0F, 180F, 0F, 0.0625F)
	}
	
	object AsItem : ItemStackTileEntityRenderer(){
		override fun renderByItem(stack: ItemStack){
			GL.pushMatrix()
			GL.disableCull()
			
			GL.translate(0.5F, 0F, 0.5F)
			GL.enableRescaleNormal()
			GL.scale(-1F, -1F, 1F)
			GL.enableAlpha()
			
			renderHead()
			
			GL.enableCull()
			GL.popMatrix()
		}
	}
	
	object AsHeadLayer{
		operator fun invoke(entity: Entity, headModel: RendererModel){
			GL.pushMatrix()
			GL.disableCull()
			
			if (entity.isSneaking){
				GL.translate(0F, 0.2F, 0F)
			}
			
			headModel.postRender(0.0625F)
			GL.color(1F, 1F, 1F, 1F)
			GL.scale(1.1875F, 1.1875F, -1.1875F)
			
			renderHead()
			
			GL.enableCull()
			GL.popMatrix()
		}
	}
}
