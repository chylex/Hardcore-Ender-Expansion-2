package chylex.hee.client.render.block
import chylex.hee.client.model.block.ModelBlockIgneousPlate
import chylex.hee.client.render.util.GL
import chylex.hee.game.block.BlockIgneousPlate
import chylex.hee.game.block.entity.TileEntityIgneousPlate
import chylex.hee.init.ModBlocks
import chylex.hee.system.Resource
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.get
import chylex.hee.system.util.getState
import chylex.hee.system.util.offsetTowards
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.GL_MODELVIEW
import org.lwjgl.opengl.GL11.GL_TEXTURE

@SideOnly(Side.CLIENT)
object RenderTileIgneousPlate : TileEntitySpecialRenderer<TileEntityIgneousPlate>(){
	private val TEX_PLATE = Resource.Custom("textures/entity/igneous_plate.png")
	
	private val COLOR_TRANSITIONS = arrayOf(
		RGB(207, 187, 161).asVec,
		RGB(247, 205,  82).asVec,
		RGB(235,  23,  23).asVec
	)
	
	private fun getInnerBoxColor(combinedHeat: Float): Vec3d{
		val index = combinedHeat.floorToInt().coerceIn(0, COLOR_TRANSITIONS.lastIndex - 1)
		val progress = combinedHeat.toDouble() - index
		
		return COLOR_TRANSITIONS[index].offsetTowards(COLOR_TRANSITIONS[index + 1], progress)
	}
	
	override fun render(tile: TileEntityIgneousPlate, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float){
		if (destroyStage >= 0){
			bindTexture(DESTROY_STAGES[destroyStage])
			GL.matrixMode(GL_TEXTURE)
			GL.pushMatrix()
			GL.scale(4F, 2F, 1F)
			GL.matrixMode(GL_MODELVIEW)
		}
		else{
			bindTexture(TEX_PLATE)
		}
		
		val state = tile.pos.getState(tile.world)
		
		if (state.block !== ModBlocks.IGNEOUS_PLATE){
			return
		}
		
		GL.pushMatrix()
		GL.translate(x, y, z)
		
		when(state[BlockIgneousPlate.FACING_NOT_DOWN]){
			UP -> {
				GL.translate(0F, 0F, 1F)
				GL.rotate(-90F, 1F, 0F, 0F)
			}
			
			NORTH -> {
				GL.translate(1F, 0F, 1F)
				GL.rotate(180F, 0F, 1F, 0F)
			}
			
			WEST -> {
				GL.translate(1F, 0F, 0F)
				GL.rotate(-90F, 0F, 1F, 0F)
			}
			
			EAST -> {
				GL.translate(0F, 0F, 1F)
				GL.rotate(90F, 0F, 1F, 0F)
			}
			
			else -> {}
		}
		
		GL.enableRescaleNormal()
		GL.color(1F, 1F, 1F, alpha)
		
		if (destroyStage < 0){
			ModelBlockIgneousPlate.renderOuterBox()
			
			val (r, g, b) = getInnerBoxColor(tile.clientCombinedHeat)
			GL.color(r.toFloat(), g.toFloat(), b.toFloat(), alpha)
		}
		
		GL.disableLighting()
		ModelBlockIgneousPlate.renderInnerBox(tile.clientThrustAnimation.get(partialTicks))
		GL.enableLighting()
		
		GL.disableRescaleNormal()
		GL.popMatrix()
		
		if (destroyStage >= 0){
			GL.matrixMode(GL_TEXTURE)
			GL.popMatrix()
			GL.matrixMode(GL_MODELVIEW)
		}
	}
}
