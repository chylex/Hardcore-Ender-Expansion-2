package chylex.hee.client.render.block
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.TESSELLATOR
import chylex.hee.client.render.util.draw
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockJarODust
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.heeTagOrNull
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11.GL_QUADS

@Sided(Side.CLIENT)
object RenderTileJarODust : TileEntityRenderer<TileEntityJarODust>(){
	private val TEX_LAYER = Resource.Custom("textures/entity/dust_layer.png")
	private const val TEX_MP = 1.6
	
	private val AABB = BlockJarODust.AABB
	
	private const val EPSILON_Y = 0.025
	private const val EPSILON_XZ = 0.005
	
	override fun render(tile: TileEntityJarODust, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int){
		GL.color(1F, 1F, 1F)
		RenderHelper.disableStandardItemLighting()
		renderLayers(tile.layers, x, y, z)
	}
	
	private fun renderLayers(layers: DustLayers, x: Double, y: Double, z: Double){ // TODO could work as FastTESR
		val contents = layers.contents.takeUnless { it.isEmpty() } ?: return
		val unit = AABB.let { it.maxY - it.minY - (EPSILON_Y * 2) } / layers.totalCapacity
		
		MC.textureManager.bindTexture(TEX_LAYER)
		
		GL.pushMatrix()
		GL.translate(x, y + AABB.minY + EPSILON_Y, z)
		
		val minX = AABB.minX + EPSILON_XZ
		val maxX = AABB.maxX - EPSILON_XZ
		
		val minZ = AABB.minZ + EPSILON_XZ
		val maxZ = AABB.maxZ - EPSILON_XZ
		
		var minY = 0.0
		
		for((index, info) in contents.withIndex()){
			val (dustType, dustAmount) = info
			
			val color = dustType.color
			val height = dustAmount * unit
			
			val maxY = minY + height
			val texMin = 0.01 + (minY * TEX_MP)
			val texMax = 0.01 + (maxY * TEX_MP)
			
			val sideR = (color[0] / 1.125F).floorToInt().coerceAtLeast(0)
			val sideG = (color[1] / 1.125F).floorToInt().coerceAtLeast(0)
			val sideB = (color[2] / 1.125F).floorToInt().coerceAtLeast(0)
			
			TESSELLATOR.draw(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR){
				pos(minX, minY, minZ).tex(texMin, 0.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(minX, minY, maxZ).tex(texMin, 1.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(minX, maxY, maxZ).tex(texMax, 1.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(minX, maxY, minZ).tex(texMax, 0.0).color(sideR, sideG, sideB, 255).endVertex()
				
				pos(maxX, minY, maxZ).tex(texMin, 0.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(maxX, minY, minZ).tex(texMin, 1.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(maxX, maxY, minZ).tex(texMax, 1.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(maxX, maxY, maxZ).tex(texMax, 0.0).color(sideR, sideG, sideB, 255).endVertex()
				
				pos(maxX, minY, minZ).tex(texMin, 0.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(minX, minY, minZ).tex(texMin, 1.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(minX, maxY, minZ).tex(texMax, 1.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(maxX, maxY, minZ).tex(texMax, 0.0).color(sideR, sideG, sideB, 255).endVertex()
				
				pos(minX, minY, maxZ).tex(texMin, 0.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(maxX, minY, maxZ).tex(texMin, 1.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(maxX, maxY, maxZ).tex(texMax, 1.0).color(sideR, sideG, sideB, 255).endVertex()
				pos(minX, maxY, maxZ).tex(texMax, 0.0).color(sideR, sideG, sideB, 255).endVertex()
			}
			
			if (index == contents.lastIndex){
				val topR = (color[0] * 1.125F).floorToInt().coerceAtMost(255)
				val topG = (color[1] * 1.125F).floorToInt().coerceAtMost(255)
				val topB = (color[2] * 1.125F).floorToInt().coerceAtMost(255)
				
				TESSELLATOR.draw(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR){
					pos(minX, maxY, minZ).tex(0.0, 0.0).color(topR, topG, topB, 255).endVertex()
					pos(minX, maxY, maxZ).tex(0.0, 1.0).color(topR, topG, topB, 255).endVertex()
					pos(maxX, maxY, maxZ).tex(1.0, 1.0).color(topR, topG, topB, 255).endVertex()
					pos(maxX, maxY, minZ).tex(1.0, 0.0).color(topR, topG, topB, 255).endVertex()
				}
			}
			
			minY = maxY
		}
		
		GL.popMatrix()
	}
	
	// TODO not implemented at the moment
	object AsItem : ItemStackTileEntityRenderer(){
		private val layers = DustLayers(TileEntityJarODust.DUST_CAPACITY)
		
		override fun renderByItem(stack: ItemStack){
			val dispatcher = MC.instance.blockRendererDispatcher
			val state = Block.getBlockFromItem(stack.item).defaultState
			val model = dispatcher.blockModelShapes.getModel(state)
			
			dispatcher.blockModelRenderer.renderModelBrightness(model, state, 1F, true)
			
			stack.heeTagOrNull?.getListOfCompounds(BlockJarODust.LAYERS_TAG)?.let {
				layers.deserializeNBT(it)
				renderLayers(layers, 0.0, 0.0, 0.0)
			}
		}
	}
}
