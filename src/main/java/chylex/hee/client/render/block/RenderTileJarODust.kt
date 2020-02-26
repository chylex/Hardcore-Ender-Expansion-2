package chylex.hee.client.render.block
import chylex.hee.HEE
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.TESSELLATOR
import chylex.hee.client.render.util.draw
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockJarODust
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.Pos
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.heeTagOrNull
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.client.model.animation.TileEntityRendererFast
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import org.lwjgl.opengl.GL11.GL_QUADS

@Sided(Side.CLIENT)
@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
object RenderTileJarODust : TileEntityRendererFast<TileEntityJarODust>(){
	private val TEX_LAYER = Resource.Custom("block/dust_layer")
	private const val TEX_MP = 1.6
	
	private lateinit var SPRITE_LAYER: TextureAtlasSprite
	
	private val AABB = BlockJarODust.AABB
	
	private const val EPSILON_Y = 0.025
	private const val EPSILON_XZ = 0.005
	
	@SubscribeEvent
	fun onTextureStitchPre(e: TextureStitchEvent.Pre){
		if (e.map.basePath == "textures"){
			e.addSprite(TEX_LAYER)
		}
	}
	
	@SubscribeEvent
	fun onTextureStitchPost(e: TextureStitchEvent.Post){
		if (e.map.basePath == "textures"){
			SPRITE_LAYER = e.map.getAtlasSprite(TEX_LAYER.toString())
		}
	}
	
	override fun renderTileEntityFast(tile: TileEntityJarODust, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, buffer: BufferBuilder){
		val world = tile.world ?: MC.world ?: return
		val pos = tile.pos
		
		renderLayers(tile.layers, x, y, z, world.getCombinedLight(pos, 0), buffer, renderBottom = false)
	}
	
	private fun renderLayers(layers: DustLayers, x: Double, y: Double, z: Double, combinedLight: Int, buffer: BufferBuilder, renderBottom: Boolean){
		val contents = layers.contents.takeUnless { it.isEmpty() } ?: return
		val unit = AABB.let { it.maxY - it.minY - (EPSILON_Y * 2) } / layers.totalCapacity
		
		val sky = (combinedLight shr 16) and 65535
		val block = combinedLight and 65535
		
		val minX = x + AABB.minX + EPSILON_XZ
		val maxX = x + AABB.maxX - EPSILON_XZ
		
		val minZ = z + AABB.minZ + EPSILON_XZ
		val maxZ = z + AABB.maxZ - EPSILON_XZ
		
		val minU = SPRITE_LAYER.minU.toDouble()
		val maxU = SPRITE_LAYER.let { it.minU + (it.maxU - it.minU) * 0.5 } // texture is 16x32 to support repeating pattern
		val minV = SPRITE_LAYER.minV.toDouble()
		val maxV = SPRITE_LAYER.maxV.toDouble()
		val texHalfSize = (maxU - minU)
		
		var relY = 0.0
		
		for((index, info) in contents.withIndex()){
			val (dustType, dustAmount) = info
			
			val color = dustType.color
			val height = dustAmount * unit
			
			val texMin = minU + (0.01 + relY * TEX_MP) * texHalfSize
			val texMax = minU + (0.01 + (relY + height) * TEX_MP) * texHalfSize
			
			val minY = y + relY + AABB.minY + EPSILON_Y
			val maxY = minY + height
			
			val sideR = (color[0] / 1.125F).floorToInt().coerceAtLeast(0)
			val sideG = (color[1] / 1.125F).floorToInt().coerceAtLeast(0)
			val sideB = (color[2] / 1.125F).floorToInt().coerceAtLeast(0)
			
			with(buffer){
				pos(minX, minY, minZ).color(sideR, sideG, sideB, 255).tex(texMin, minV).lightmap(sky, block).endVertex()
				pos(minX, minY, maxZ).color(sideR, sideG, sideB, 255).tex(texMin, maxV).lightmap(sky, block).endVertex()
				pos(minX, maxY, maxZ).color(sideR, sideG, sideB, 255).tex(texMax, maxV).lightmap(sky, block).endVertex()
				pos(minX, maxY, minZ).color(sideR, sideG, sideB, 255).tex(texMax, minV).lightmap(sky, block).endVertex()
				
				pos(maxX, minY, maxZ).color(sideR, sideG, sideB, 255).tex(texMin, minV).lightmap(sky, block).endVertex()
				pos(maxX, minY, minZ).color(sideR, sideG, sideB, 255).tex(texMin, maxV).lightmap(sky, block).endVertex()
				pos(maxX, maxY, minZ).color(sideR, sideG, sideB, 255).tex(texMax, maxV).lightmap(sky, block).endVertex()
				pos(maxX, maxY, maxZ).color(sideR, sideG, sideB, 255).tex(texMax, minV).lightmap(sky, block).endVertex()
				
				pos(maxX, minY, minZ).color(sideR, sideG, sideB, 255).tex(texMin, minV).lightmap(sky, block).endVertex()
				pos(minX, minY, minZ).color(sideR, sideG, sideB, 255).tex(texMin, maxV).lightmap(sky, block).endVertex()
				pos(minX, maxY, minZ).color(sideR, sideG, sideB, 255).tex(texMax, maxV).lightmap(sky, block).endVertex()
				pos(maxX, maxY, minZ).color(sideR, sideG, sideB, 255).tex(texMax, minV).lightmap(sky, block).endVertex()
				
				pos(minX, minY, maxZ).color(sideR, sideG, sideB, 255).tex(texMin, minV).lightmap(sky, block).endVertex()
				pos(maxX, minY, maxZ).color(sideR, sideG, sideB, 255).tex(texMin, maxV).lightmap(sky, block).endVertex()
				pos(maxX, maxY, maxZ).color(sideR, sideG, sideB, 255).tex(texMax, maxV).lightmap(sky, block).endVertex()
				pos(minX, maxY, maxZ).color(sideR, sideG, sideB, 255).tex(texMax, minV).lightmap(sky, block).endVertex()
			}
			
			if (index == 0 && renderBottom){
				val bottomR = color[0]
				val bottomG = color[1]
				val bottomB = color[2]
				
				with(buffer){
					pos(maxX, minY, minZ).color(bottomR, bottomG, bottomB, 255).tex(maxU, minV).lightmap(sky, block).endVertex()
					pos(maxX, minY, maxZ).color(bottomR, bottomG, bottomB, 255).tex(maxU, maxV).lightmap(sky, block).endVertex()
					pos(minX, minY, maxZ).color(bottomR, bottomG, bottomB, 255).tex(minU, maxV).lightmap(sky, block).endVertex()
					pos(minX, minY, minZ).color(bottomR, bottomG, bottomB, 255).tex(minU, minV).lightmap(sky, block).endVertex()
				}
			}
			
			if (index == contents.lastIndex){
				val topR = (color[0] * 1.125F).floorToInt().coerceAtMost(255)
				val topG = (color[1] * 1.125F).floorToInt().coerceAtMost(255)
				val topB = (color[2] * 1.125F).floorToInt().coerceAtMost(255)
				
				with(buffer){
					pos(minX, maxY, minZ).color(topR, topG, topB, 255).tex(minU, minV).lightmap(sky, block).endVertex()
					pos(minX, maxY, maxZ).color(topR, topG, topB, 255).tex(minU, maxV).lightmap(sky, block).endVertex()
					pos(maxX, maxY, maxZ).color(topR, topG, topB, 255).tex(maxU, maxV).lightmap(sky, block).endVertex()
					pos(maxX, maxY, minZ).color(topR, topG, topB, 255).tex(maxU, minV).lightmap(sky, block).endVertex()
				}
			}
			
			relY += height
		}
	}
	
	@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
	object AsItem : ItemStackTileEntityRenderer(){
		private val RESOURCE_MODEL = Resource.Custom("block/jar_o_dust_simple")
		private lateinit var MODEL: IBakedModel
		
		@SubscribeEvent
		fun onRegisterModels(@Suppress("UNUSED_PARAMETER") e: ModelRegistryEvent){
			ModelLoader.addSpecialModel(RESOURCE_MODEL)
		}
		
		@SubscribeEvent
		fun onModelBake(e: ModelBakeEvent){
			MODEL = e.modelRegistry.getValue(RESOURCE_MODEL)
		}
		
		private val layers = DustLayers(TileEntityJarODust.DUST_CAPACITY)
		
		override fun renderByItem(stack: ItemStack){
			val nbt = stack.heeTagOrNull?.getListOfCompounds(BlockJarODust.LAYERS_TAG)
			val player = MC.player ?: return
			
			GL.enableCull()
			
			if (nbt != null){
				layers.deserializeNBT(nbt)
				RenderHelper.disableStandardItemLighting()
				
				TESSELLATOR.draw(GL_QUADS, DefaultVertexFormats.BLOCK){
					renderLayers(layers, 0.0, 0.0, 0.0, world.getCombinedLight(Pos(player), 0), this, renderBottom = true)
				}
				
				RenderHelper.enableStandardItemLighting()
			}
			
			MC.instance.blockRendererDispatcher.blockModelRenderer.renderModelBrightnessColor(MODEL, 1F, 1F, 1F, 1F)
			GL.disableCull()
		}
	}
}
