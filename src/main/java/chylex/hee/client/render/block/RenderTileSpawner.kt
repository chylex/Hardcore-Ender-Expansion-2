package chylex.hee.client.render.block
import chylex.hee.client.MC
import chylex.hee.client.render.gl.rotateX
import chylex.hee.client.render.gl.rotateY
import chylex.hee.client.render.gl.scale
import chylex.hee.client.render.gl.translateY
import chylex.hee.game.block.entity.base.TileEntityBaseSpawner
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import kotlin.math.max

@Sided(Side.CLIENT)
class RenderTileSpawner(dispatcher: TileEntityRendererDispatcher) : TileEntityRenderer<TileEntityBaseSpawner>(dispatcher){
	override fun render(tile: TileEntityBaseSpawner, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int){
		val entity = tile.clientEntity
		val scale = 0.53125F / max(entity.width, entity.height).coerceAtLeast(1F)
		
		matrix.push()
		matrix.translate(0.5, 0.4, 0.5)
		matrix.rotateY(tile.clientRotation.get(partialTicks) * 10F)
		matrix.translateY(-0.2)
		matrix.rotateX(-30F)
		matrix.scale(scale)
		
		MC.renderManager.renderEntityStatic(entity, 0.0, 0.0, 0.0, 0F, partialTicks, matrix, buffer, combinedLight)
		
		matrix.pop()
	}
}
