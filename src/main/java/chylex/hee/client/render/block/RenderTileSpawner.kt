package chylex.hee.client.render.block
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.game.block.entity.base.TileEntityBaseSpawner
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import kotlin.math.max

@Sided(Side.CLIENT)
object RenderTileSpawner : TileEntityRenderer<TileEntityBaseSpawner>(){
	override fun render(tile: TileEntityBaseSpawner, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int){
		val entity = tile.clientEntity
		val scale = 0.53125F / max(entity.width, entity.height).coerceAtLeast(1F)
		
		GL.pushMatrix()
		GL.translate(x + 0.5, y + 0.4, z + 0.5)
		GL.rotate(tile.clientRotation.get(partialTicks) * 10F, 0F, 1F, 0F)
		GL.translate(0F, -0.2F, 0F)
		GL.rotate(-30F, 1F, 0F, 0F)
		GL.scale(scale, scale, scale)
		
		entity.setLocationAndAngles(x, y, z, 0F, 0F)
		MC.renderManager.renderEntity(entity, 0.0, 0.0, 0.0, 0F, partialTicks, false)
		
		GL.popMatrix()
	}
}
