package chylex.hee.client.render.block
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.system.Resource
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.model.ModelSkeletonHead
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object RenderTileEndermanHead{
	private val TEX_ENDERMAN = Resource.Custom("textures/entity/enderman_head.png")
	private val MODEL_HEAD = ModelSkeletonHead(0, 0, 64, 32)
	
	@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
	private fun renderHead(){
		MC.textureManager.bindTexture(TEX_ENDERMAN)
		MODEL_HEAD.render(null, 0F, 0F, 0F, 180F, 0F, 0.0625F)
	}
	
	object AsItem : TileEntityItemStackRenderer(){
		override fun renderByItem(stack: ItemStack, partialTicks: Float){
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
		operator fun invoke(entity: Entity, headModel: ModelRenderer){
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
