package chylex.hee.game.render.block
import chylex.hee.game.block.entity.TileEntityBaseChest
import chylex.hee.game.render.util.GL
import net.minecraft.client.model.ModelChest
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.WEST
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.GL_MODELVIEW
import org.lwjgl.opengl.GL11.GL_TEXTURE
import kotlin.math.PI
import kotlin.math.pow

@SideOnly(Side.CLIENT)
abstract class RenderTileAbstractChest<T : TileEntityBaseChest> : TileEntitySpecialRenderer<T>(){
	protected abstract val texture: ResourceLocation
	
	private val modelChest = ModelChest()
	
	override fun render(tile: T, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float){
		if (destroyStage >= 0){
			bindTexture(DESTROY_STAGES[destroyStage])
			GL.matrixMode(GL_TEXTURE)
			GL.pushMatrix()
			GL.scale(4F, 4F, 1F)
			GL.translate(0.0625F, 0.0625F, 0.0625F)
			GL.matrixMode(GL_MODELVIEW)
		}
		else{
			bindTexture(texture)
		}
		
		GL.pushMatrix()
		GL.enableRescaleNormal()
		GL.color(1F, 1F, 1F, alpha)
		GL.translate(x, y + 1F, z + 1F)
		GL.scale(1F, -1F, -1F)
		GL.translate(0.5F, 0.5F, 0.5F)
		
		val rotation = when(tile.facing){
			EAST  -> 270F
			NORTH -> 180F
			WEST  ->  90F
			else  ->   0F
		}
		
		GL.rotate(rotation, 0F, 1F, 0F)
		GL.translate(-0.5F, -0.5F, -0.5F)
		
		modelChest.chestLid.rotateAngleX = -(1F - (1F - tile.lidAngle.get(partialTicks)).pow(3)) * PI.toFloat() * 0.5F
		modelChest.renderAll()
		
		GL.disableRescaleNormal()
		GL.popMatrix()
		GL.color(1F, 1F, 1F, 1F)
		
		if (destroyStage >= 0){
			GL.matrixMode(GL_TEXTURE)
			GL.popMatrix()
			GL.matrixMode(GL_MODELVIEW)
		}
	}
}
