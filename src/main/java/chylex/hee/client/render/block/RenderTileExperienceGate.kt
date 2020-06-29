package chylex.hee.client.render.block
import chylex.hee.HEE
import chylex.hee.client.render.util.RenderStateBuilder
import chylex.hee.client.render.util.RenderStateBuilder.Companion.ALPHA_CUTOUT
import chylex.hee.client.render.util.RenderStateBuilder.Companion.LIGHTMAP_ENABLED
import chylex.hee.client.render.util.RenderStateBuilder.Companion.SHADE_ENABLED
import chylex.hee.game.block.entity.TileEntityExperienceGate
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.floorToInt
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.Matrix4f
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.inventory.container.PlayerContainer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import org.lwjgl.opengl.GL11

@Sided(Side.CLIENT)
class RenderTileExperienceGate(dispatcher: TileEntityRendererDispatcher) : TileEntityRenderer<TileEntityExperienceGate>(dispatcher){
	@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
	companion object{
		private const val SPRITE_COUNT = 40
		
		private val TEX = Array(SPRITE_COUNT){ Resource.Custom("block/experience_gate_top_bar_$it") }
		private val SPRITES = mutableListOf<TextureAtlasSprite>()
		
		private val RENDER_TYPE_BAR = with(RenderStateBuilder()){
			tex(PlayerContainer.LOCATION_BLOCKS_TEXTURE, mipmap = true)
			alpha(ALPHA_CUTOUT)
			shade(SHADE_ENABLED)
			lightmap(LIGHTMAP_ENABLED)
			buildType("hee:experience_gate_bar", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, drawMode = GL11.GL_QUADS, bufferSize = 256)
		}
		
		private val FRAMES_CORNER = intArrayOf(9, 10, 11, 12, 13, 14, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39)
		private val FRAMES_STRAIGHT = intArrayOf(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24)
		
		private val FRAMES = arrayOf(
			intArrayOf(0, 1, 2),
			FRAMES_STRAIGHT,
			FRAMES_CORNER,
			FRAMES_STRAIGHT,
			intArrayOf(5, 6, 7, 8, 4),
			FRAMES_STRAIGHT,
			FRAMES_CORNER,
			FRAMES_STRAIGHT,
			intArrayOf(3, 4)
		)
		
		private val FRAME_COUNT = 1 + FRAMES.sumBy { it.size }
		private val FRAME_OFFSETS = FRAMES.indices.map { index -> 1 + FRAMES.take(index).sumBy { it.size } }.toIntArray()
		
		@SubscribeEvent
		fun onTextureStitchPre(e: TextureStitchEvent.Pre){
			if (e.map.textureLocation == PlayerContainer.LOCATION_BLOCKS_TEXTURE){
				with(e){
					TEX.forEach { addSprite(it) }
				}
			}
		}
		
		@SubscribeEvent
		fun onTextureStitchPost(e: TextureStitchEvent.Post){
			if (e.map.textureLocation == PlayerContainer.LOCATION_BLOCKS_TEXTURE){
				SPRITES.clear()
				
				with(e.map){
					TEX.forEach { SPRITES.add(getSprite(it)) }
				}
			}
		}
		
		private fun getTexture(index: Int, frame: Int): TextureAtlasSprite?{
			return FRAMES[index].getOrNull((frame - FRAME_OFFSETS[index]).coerceAtMost(FRAMES[index].lastIndex))?.let(SPRITES::getOrNull)
		}
	}
	
	override fun render(tile: TileEntityExperienceGate, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int){
		val world = tile.world ?: return
		val pos = tile.pos
		
		val builder = buffer.getBuffer(RENDER_TYPE_BAR)
		val mat = matrix.last.matrix
		
		val progress = tile.chargeProgress
		val frame = if (progress == 0F) 0 else 1 + (progress * (FRAME_COUNT - 2)).floorToInt()
		
		val topLeftBlock = getTexture(8, frame) ?: getTexture(0, frame)
		
		topLeftBlock?.let         { builder.renderTextureAt(mat, -1F, -1F, world, pos.add(-1, 1, -1), it, 0b0000, combinedOverlay) }
		getTexture(1, frame)?.let { builder.renderTextureAt(mat,  0F, -1F, world, pos.add( 0, 1, -1), it, 0b0111, combinedOverlay) }
		getTexture(2, frame)?.let { builder.renderTextureAt(mat,  1F, -1F, world, pos.add( 1, 1, -1), it, 0b1001, combinedOverlay) }
		getTexture(3, frame)?.let { builder.renderTextureAt(mat,  1F,  0F, world, pos.add( 1, 1,  0), it, 0b0110, combinedOverlay) }
		getTexture(4, frame)?.let { builder.renderTextureAt(mat,  1F,  1F, world, pos.add( 1, 1,  1), it, 0b0110, combinedOverlay) }
		getTexture(5, frame)?.let { builder.renderTextureAt(mat,  0F,  1F, world, pos.add( 0, 1,  1), it, 0b0001, combinedOverlay) }
		getTexture(6, frame)?.let { builder.renderTextureAt(mat, -1F,  1F, world, pos.add(-1, 1,  1), it, 0b1111, combinedOverlay) }
		getTexture(7, frame)?.let { builder.renderTextureAt(mat, -1F,  0F, world, pos.add(-1, 1,  0), it, 0b0000, combinedOverlay) }
	}
	
	private fun IVertexBuilder.renderTextureAt(mat: Matrix4f, x: Float, z: Float, world: World, pos: BlockPos, tex: TextureAtlasSprite, rot: Int, overlay: Int){
		val rotX = (((rot shr 1) and 1) - 0.5F) * 1.002F
		val rotZ = (((rot shr 2) and 1) - 0.5F) * 1.002F
		
		val u1 = tex.minU
		val u2 = tex.maxU
		val v1 = tex.minV
		val v2 = tex.maxV
		
		val c = if (rot and 0b1000 != 0){
			floatArrayOf(u2, u1, u1, u2, v1, v1, v2, v2)
		}
		else if (rot and 0b0001 == 0){
			floatArrayOf(u2, u2, u1, u1, v2, v1, v1, v2)
		}
		else{
			floatArrayOf(u2, u1, u1, u2, v2, v2, v1, v1)
		}
		
		val light = WorldRenderer.getCombinedLight(world, pos)
		
		this.pos(mat, x + 0.5F - rotX, 1.001F, z + 0.5F - rotZ).color(255, 255, 255, 255).tex(c[0], c[4]).lightmap(light).overlay(overlay).endVertex()
		this.pos(mat, x + 0.5F - rotX, 1.001F, z + 0.5F + rotZ).color(255, 255, 255, 255).tex(c[1], c[5]).lightmap(light).overlay(overlay).endVertex()
		this.pos(mat, x + 0.5F + rotX, 1.001F, z + 0.5F + rotZ).color(255, 255, 255, 255).tex(c[2], c[6]).lightmap(light).overlay(overlay).endVertex()
		this.pos(mat, x + 0.5F + rotX, 1.001F, z + 0.5F - rotZ).color(255, 255, 255, 255).tex(c[3], c[7]).lightmap(light).overlay(overlay).endVertex()
	}
}
