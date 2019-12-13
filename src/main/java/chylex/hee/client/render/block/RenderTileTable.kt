package chylex.hee.client.render.block
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.ItemRenderHelper
import chylex.hee.client.render.util.TESSELLATOR
import chylex.hee.client.render.util.draw
import chylex.hee.client.util.MC
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.Vec3
import chylex.hee.system.util.addY
import chylex.hee.system.util.center
import chylex.hee.system.util.color.IntColor.Companion.RGBA
import chylex.hee.system.util.getTile
import chylex.hee.system.util.lookPosVec
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.GUI
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemStack
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.client.model.pipeline.LightUtil
import org.lwjgl.opengl.GL11.GL_QUADS

@Sided(Side.CLIENT)
object RenderTileTable : TileEntitySpecialRenderer<TileEntityBaseTable>(){
	private val COLOR = RGBA(180, 180, 180, 120).i
	private const val Y_OFFSET = 0.8F
	
	override fun render(tile: TileEntityBaseTable, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float){
		val dustType = tile.tableDustType ?: return
		
		if (tile.pos.up().getTile<TileEntityJarODust>(world)?.layers?.getDustType(DustLayers.Side.BOTTOM) == dustType){
			return
		}
		
		ItemRenderHelper.beginItemModel()
		GL.pushMatrix()
		GL.translate(x, y, z)
		GL.pushMatrix()
		
		val rotation = (MC.systemTime % 360000L) / 25F
		val center = tile.pos.center.addY(Y_OFFSET.toDouble())
		val flip = if (center.subtract(MC.player!!.lookPosVec).dotProduct(Vec3.fromYaw(360F - rotation)) > 0.0) 180F else 0F
		
		GL.translate(0.5F, 0.5F + Y_OFFSET, 0.5F)
		GL.rotate(rotation + flip, 0F, 1F, 0F)
		GL.scale(0.5F, 0.5F, 0.02F)
		
		val itemStack = ItemStack(dustType.item)
		val itemModel = ForgeHooksClient.handleCameraTransforms(ItemRenderHelper.getItemModel(itemStack), GUI, false)
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680F, 0F)
		GL.translate(-0.5F, -0.5F, -0.5F)
		
		TESSELLATOR.draw(GL_QUADS, DefaultVertexFormats.ITEM){
			val quads = itemModel.getQuads(null, null, 0L)
			
			for(quad in quads){
				LightUtil.renderQuadColor(this, quad, COLOR)
			}
		}
		
		GL.popMatrix()
		GL.popMatrix()
		ItemRenderHelper.endItemModel()
	}
}
