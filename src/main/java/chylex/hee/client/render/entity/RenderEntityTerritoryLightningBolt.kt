package chylex.hee.client.render.entity
import chylex.hee.game.entity.effect.EntityTerritoryLightningBolt
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.Matrix4f
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.inventory.container.PlayerContainer
import net.minecraft.util.ResourceLocation
import java.util.Random

@Sided(Side.CLIENT)
class RenderEntityTerritoryLightningBolt(manager: EntityRendererManager) : EntityRenderer<EntityTerritoryLightningBolt>(manager){
	override fun render(entity: EntityTerritoryLightningBolt, yaw: Float, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int){
		val xCoords = FloatArray(8)
		val zCoords = FloatArray(8)
		var xOffset = 0F
		var zOffset = 0F
		
		run {
			val rand = Random(entity.boltVertex)
			
			for(i in 7 downTo 0){
				xCoords[i] = xOffset
				zCoords[i] = zOffset
				xOffset += rand.nextInt(11) - 5
				zOffset += rand.nextInt(11) - 5
			}
		}
		
		val builder = buffer.getBuffer(RenderType.getLightning())
		val mat = matrix.last.matrix
		
		for(iter in 0..3){
			val rand = Random(entity.boltVertex)
			
			for(branch in 0..2){
				val i1 = 7 - branch
				val i2 = if (branch > 0) i1 - 2 else 0
				
				var x = xCoords[i1] - xOffset
				var z = zCoords[i1] - zOffset
				
				for(y in i1 downTo i2){
					val origX = x
					val origZ = z
					
					if (branch == 0){
						x += rand.nextInt(11) - 5
						z += rand.nextInt(11) - 5
					}
					else{
						x += rand.nextInt(31) - 15
						z += rand.nextInt(31) - 15
					}
					
					val off1 = (0.1F + (iter * 0.2F)) * (if (branch == 0) ((y * 0.1F + 1F)) else 1F)
					val off2 = (0.1F + (iter * 0.2F)) * (if (branch == 0) (((y - 1) * 0.1F + 1F)) else 1F)
					
					addVertex(mat, builder, x, z, y, origX, origZ, off1, off2, offX1 = false, offZ1 = false, offX2 = true, offZ2 = false)
					addVertex(mat, builder, x, z, y, origX, origZ, off1, off2, offX1 = true, offZ1 = false, offX2 = true, offZ2 = true)
					addVertex(mat, builder, x, z, y, origX, origZ, off1, off2, offX1 = true, offZ1 = true, offX2 = false, offZ2 = true)
					addVertex(mat, builder, x, z, y, origX, origZ, off1, off2, offX1 = false, offZ1 = true, offX2 = false, offZ2 = false)
				}
			}
		}
	}
	
	private fun addVertex(mat: Matrix4f, builder: IVertexBuilder, x1: Float, z1: Float, y: Int, x2: Float, z2: Float, off1: Float, off2: Float, offX1: Boolean, offZ1: Boolean, offX2: Boolean, offZ2: Boolean){
		builder.pos(mat, x1 + if (offX1) off2 else -off2, (y * 16).toFloat(), z1 + if (offZ1) off2 else -off2).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex()
		builder.pos(mat, x2 + if (offX1) off1 else -off1, ((y + 1) * 16).toFloat(), z2 + if (offZ1) off1 else -off1).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex()
		builder.pos(mat, x2 + if (offX2) off1 else -off1, ((y + 1) * 16).toFloat(), z2 + if (offZ2) off1 else -off1).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex()
		builder.pos(mat, x1 + if (offX2) off2 else -off2, (y * 16).toFloat(), z1 + if (offZ2) off2 else -off2).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex()
	}
	
	override fun getEntityTexture(entity: EntityTerritoryLightningBolt): ResourceLocation{
		return PlayerContainer.LOCATION_BLOCKS_TEXTURE
	}
}
