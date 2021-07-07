package chylex.hee.client.render.block

import chylex.hee.client.model.block.ModelBlockIgneousPlate
import chylex.hee.client.render.RenderStateBuilder
import chylex.hee.client.render.RenderStateBuilder.Companion.BLEND_NONE
import chylex.hee.client.render.RenderStateBuilder.Companion.LIGHTING_DISABLED
import chylex.hee.client.render.RenderStateBuilder.Companion.LIGHTMAP_ENABLED
import chylex.hee.client.render.RenderStateBuilder.Companion.OVERLAY_ENABLED
import chylex.hee.client.render.util.rotateX
import chylex.hee.client.render.util.rotateY
import chylex.hee.client.render.util.translateX
import chylex.hee.client.render.util.translateZ
import chylex.hee.game.Resource
import chylex.hee.game.block.BlockIgneousPlate
import chylex.hee.game.block.entity.TileEntityIgneousPlate
import chylex.hee.game.world.util.getState
import chylex.hee.init.ModBlocks
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.lerpTowards
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.UP
import net.minecraft.util.Direction.WEST
import net.minecraft.util.math.vector.Vector3d
import org.lwjgl.opengl.GL11

@Sided(Side.CLIENT)
class RenderTileIgneousPlate(dispatcher: TileEntityRendererDispatcher) : TileEntityRenderer<TileEntityIgneousPlate>(dispatcher) {
	private companion object {
		private val TEX_PLATE = Resource.Custom("textures/entity/igneous_plate.png")
		
		private val RENDER_TYPE_OUTER = RenderType.getEntitySolid(TEX_PLATE)
		private val RENDER_TYPE_INNER = with(RenderStateBuilder()) {
			tex(TEX_PLATE)
			blend(BLEND_NONE)
			lighting(LIGHTING_DISABLED)
			lightmap(LIGHTMAP_ENABLED)
			overlay(OVERLAY_ENABLED)
			buildType("hee:igneous_plate_inner", DefaultVertexFormats.ENTITY, GL11.GL_QUADS, bufferSize = 256, useDelegate = true)
		}
		
		private val COLOR_TRANSITIONS = arrayOf(
			RGB(207, 187, 161).asVec,
			RGB(247, 205,  82).asVec,
			RGB(235,  23,  23).asVec
		)
		
		private fun getInnerBoxColor(combinedHeat: Float): Vector3d {
			val index = combinedHeat.floorToInt().coerceIn(0, COLOR_TRANSITIONS.lastIndex - 1)
			val progress = combinedHeat.toDouble() - index
			
			return COLOR_TRANSITIONS[index].lerpTowards(COLOR_TRANSITIONS[index + 1], progress)
		}
	}
	
	override fun render(tile: TileEntityIgneousPlate, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int) {
		val state = tile.world?.let(tile.pos::getState)
		
		if (state?.block !== ModBlocks.IGNEOUS_PLATE) {
			return
		}
		
		matrix.push()
		
		when(state[BlockIgneousPlate.FACING_NOT_DOWN]) {
			UP -> {
				matrix.translateZ(1.0)
				matrix.rotateX(-90F)
			}
			
			NORTH -> {
				matrix.translate(1.0, 0.0, 1.0)
				matrix.rotateY(180F)
			}
			
			WEST -> {
				matrix.translateX(1.0)
				matrix.rotateY(-90F)
			}
			
			EAST -> {
				matrix.translateZ(1.0)
				matrix.rotateY(90F)
			}
			
			else -> {}
		}
		
		ModelBlockIgneousPlate.render(matrix, buffer.getBuffer(RENDER_TYPE_OUTER), combinedLight, combinedOverlay, 1F, 1F, 1F, 1F)
		ModelBlockIgneousPlate.renderInnerBox(matrix, buffer.getBuffer(RENDER_TYPE_INNER), combinedLight, combinedOverlay, getInnerBoxColor(tile.clientCombinedHeat), tile.clientThrustAnimation.get(partialTicks))
		
		matrix.pop()
	}
}
