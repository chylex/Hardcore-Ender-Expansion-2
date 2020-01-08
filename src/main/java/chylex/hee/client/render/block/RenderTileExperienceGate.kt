package chylex.hee.client.render.block
import chylex.hee.HEE
import chylex.hee.game.block.entity.TileEntityExperienceGate
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.floorToInt
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.animation.TileEntityRendererFast
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@Sided(Side.CLIENT)
@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
object RenderTileExperienceGate : TileEntityRendererFast<TileEntityExperienceGate>(){
	private const val SPRITE_COUNT = 40
	
	private val TEX = Array(SPRITE_COUNT){ Resource.Custom("block/experience_gate_top_bar_$it") }
	private val SPRITES = mutableListOf<TextureAtlasSprite>()
	
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
	
	@JvmStatic
	@SubscribeEvent
	fun onTextureStitchPre(e: TextureStitchEvent.Pre){
		if (e.map.basePath != "textures/block"){
			return
		}
		
		with(e){
			TEX.forEach { addSprite(it) }
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onTextureStitchPost(e: TextureStitchEvent.Post){
		if (e.map.basePath != "textures/block"){
			return
		}
		
		SPRITES.clear()
		
		with(e.map){
			TEX.forEach { SPRITES.add(getAtlasSprite(it.toString())) }
		}
	}
	
	private fun getTexture(index: Int, frame: Int): TextureAtlasSprite?{
		return FRAMES[index].getOrNull((frame - FRAME_OFFSETS[index]).coerceAtMost(FRAMES[index].lastIndex))?.let(SPRITES::getOrNull)
	}
	
	override fun renderTileEntityFast(tile: TileEntityExperienceGate, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, buffer: BufferBuilder){
		val world = tile.world ?: return
		val pos = tile.pos
		
		val progress = tile.chargeProgress
		val frame = if (progress == 0F) 0 else 1 + (progress * (FRAME_COUNT - 2)).floorToInt()
		
		val topLeftBlock = getTexture(8, frame) ?: getTexture(0, frame)
		
		topLeftBlock?.let         { buffer.renderTextureAt(x - 1.0, y, z - 1.0, world, pos.add(-1, 1, -1), it, 0b000) }
		getTexture(1, frame)?.let { buffer.renderTextureAt(x + 0.0, y, z - 1.0, world, pos.add( 0, 1, -1), it, 0b111) }
		getTexture(2, frame)?.let { buffer.renderTextureAt(x + 1.0, y, z - 1.0, world, pos.add( 1, 1, -1), it, 0b011) }
		getTexture(3, frame)?.let { buffer.renderTextureAt(x + 1.0, y, z + 0.0, world, pos.add( 1, 1,  0), it, 0b110) }
		getTexture(4, frame)?.let { buffer.renderTextureAt(x + 1.0, y, z + 1.0, world, pos.add( 1, 1,  1), it, 0b110) }
		getTexture(5, frame)?.let { buffer.renderTextureAt(x + 0.0, y, z + 1.0, world, pos.add( 0, 1,  1), it, 0b001) }
		getTexture(6, frame)?.let { buffer.renderTextureAt(x - 1.0, y, z + 1.0, world, pos.add(-1, 1,  1), it, 0b101) }
		getTexture(7, frame)?.let { buffer.renderTextureAt(x - 1.0, y, z + 0.0, world, pos.add(-1, 1,  0), it, 0b000) }
	}
	
	private fun BufferBuilder.renderTextureAt(x: Double, y: Double, z: Double, world: World, pos: BlockPos, tex: TextureAtlasSprite, rot: Int){
		val rotX = (((rot shr 1) and 1) - 0.5) * 1.002
		val rotZ = (((rot shr 2) and 1) - 0.5) * 1.002
		
		val u1 = tex.minU.toDouble()
		val u2 = tex.maxU.toDouble()
		val v1 = tex.minV.toDouble()
		val v2 = tex.maxV.toDouble()
		
		val c = if (rot and 1 == 0){
			doubleArrayOf(u1, u2, u2, u1, v1, v1, v2, v2)
		}
		else{
			doubleArrayOf(u1, u1, u2, u2, v1, v2, v2, v1)
		}
		
		val light = world.getCombinedLight(pos, 0)
		val sky = (light shr 16) and 65535
		val block = light and 65535
		
		this.pos(x + 0.5 + rotX, y + 1.001, z + 0.5 + rotZ).color(255, 255, 255, 255).tex(c[0], c[4]).lightmap(sky, block).endVertex()
		this.pos(x + 0.5 - rotX, y + 1.001, z + 0.5 + rotZ).color(255, 255, 255, 255).tex(c[1], c[5]).lightmap(sky, block).endVertex()
		this.pos(x + 0.5 - rotX, y + 1.001, z + 0.5 - rotZ).color(255, 255, 255, 255).tex(c[2], c[6]).lightmap(sky, block).endVertex()
		this.pos(x + 0.5 + rotX, y + 1.001, z + 0.5 - rotZ).color(255, 255, 255, 255).tex(c[3], c[7]).lightmap(sky, block).endVertex()
	}
}
