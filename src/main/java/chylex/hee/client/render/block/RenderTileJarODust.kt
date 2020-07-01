package chylex.hee.client.render.block
import chylex.hee.HEE
import chylex.hee.client.render.util.RenderStateBuilder
import chylex.hee.client.render.util.RenderStateBuilder.Companion.LIGHTMAP_ENABLED
import chylex.hee.client.render.util.RenderStateBuilder.Companion.SHADE_ENABLED
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockJarODust
import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.offsetTowards
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.Atlases
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.inventory.container.PlayerContainer
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import org.lwjgl.opengl.GL11

@Sided(Side.CLIENT)
class RenderTileJarODust(dispatcher: TileEntityRendererDispatcher) : TileEntityRenderer<TileEntityJarODust>(dispatcher){
	@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
	companion object{
		private val TEX_LAYER = Resource.Custom("block/dust_layer")
		private const val TEX_MP = 1.6
		
		private lateinit var SPRITE_LAYER: TextureAtlasSprite
		
		private val RENDER_TYPE_LAYERS = with(RenderStateBuilder()){
			tex(PlayerContainer.LOCATION_BLOCKS_TEXTURE, mipmap = true)
			shade(SHADE_ENABLED)
			lightmap(LIGHTMAP_ENABLED)
			buildType("hee:jar_layers", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, drawMode = GL11.GL_QUADS, bufferSize = 256)
		}
		
		private val AABB = BlockJarODust.AABB
		
		private const val EPSILON_Y = 0.025
		private const val EPSILON_XZ = 0.005
		
		@SubscribeEvent
		fun onTextureStitchPre(e: TextureStitchEvent.Pre){
			if (e.map.textureLocation == PlayerContainer.LOCATION_BLOCKS_TEXTURE){
				e.addSprite(TEX_LAYER)
			}
		}
		
		@SubscribeEvent
		fun onTextureStitchPost(e: TextureStitchEvent.Post){
			if (e.map.textureLocation == PlayerContainer.LOCATION_BLOCKS_TEXTURE){
				SPRITE_LAYER = e.map.getSprite(TEX_LAYER)
			}
		}
		
		private fun renderLayers(layers: DustLayers, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int, renderBottom: Boolean){
			val contents = layers.contents.takeUnless { it.isEmpty() } ?: return
			val unit = AABB.let { it.maxY - it.minY - (EPSILON_Y * 2) } / layers.totalCapacity
			
			val builder = buffer.getBuffer(RENDER_TYPE_LAYERS)
			val mat = matrix.last.matrix
			
			val minX = (AABB.minX + EPSILON_XZ).toFloat()
			val maxX = (AABB.maxX - EPSILON_XZ).toFloat()
			
			val minZ = (AABB.minZ + EPSILON_XZ).toFloat()
			val maxZ = (AABB.maxZ - EPSILON_XZ).toFloat()
			
			val minU = SPRITE_LAYER.minU
			val maxU = SPRITE_LAYER.let { offsetTowards(it.minU, it.maxU, 0.5F) }.toFloat() // texture is 16x32 to support repeating pattern
			val minV = SPRITE_LAYER.minV
			val maxV = SPRITE_LAYER.maxV
			val texHalfSize = (maxU - minU)
			
			var relY = 0.0
			
			for((index, info) in contents.withIndex()){
				val (dustType, dustAmount) = info
				
				val color = dustType.color
				val height = dustAmount * unit
				
				val texMin = minU + ((0.01 + relY * TEX_MP) * texHalfSize).toFloat()
				val texMax = minU + ((0.01 + (relY + height) * TEX_MP) * texHalfSize).toFloat()
				
				val minY = (relY + AABB.minY + EPSILON_Y).toFloat()
				val maxY = (minY + height).toFloat()
				
				val sideR = (color[0] / 1.125F).floorToInt().coerceAtLeast(0)
				val sideG = (color[1] / 1.125F).floorToInt().coerceAtLeast(0)
				val sideB = (color[2] / 1.125F).floorToInt().coerceAtLeast(0)
				
				with(builder){
					pos(mat, minX, minY, minZ).color(sideR, sideG, sideB, 255).tex(texMin, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, minX, minY, maxZ).color(sideR, sideG, sideB, 255).tex(texMin, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, minX, maxY, maxZ).color(sideR, sideG, sideB, 255).tex(texMax, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, minX, maxY, minZ).color(sideR, sideG, sideB, 255).tex(texMax, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					
					pos(mat, maxX, minY, maxZ).color(sideR, sideG, sideB, 255).tex(texMin, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, maxX, minY, minZ).color(sideR, sideG, sideB, 255).tex(texMin, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, maxX, maxY, minZ).color(sideR, sideG, sideB, 255).tex(texMax, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, maxX, maxY, maxZ).color(sideR, sideG, sideB, 255).tex(texMax, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					
					pos(mat, maxX, minY, minZ).color(sideR, sideG, sideB, 255).tex(texMin, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, minX, minY, minZ).color(sideR, sideG, sideB, 255).tex(texMin, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, minX, maxY, minZ).color(sideR, sideG, sideB, 255).tex(texMax, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, maxX, maxY, minZ).color(sideR, sideG, sideB, 255).tex(texMax, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					
					pos(mat, minX, minY, maxZ).color(sideR, sideG, sideB, 255).tex(texMin, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, maxX, minY, maxZ).color(sideR, sideG, sideB, 255).tex(texMin, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, maxX, maxY, maxZ).color(sideR, sideG, sideB, 255).tex(texMax, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					pos(mat, minX, maxY, maxZ).color(sideR, sideG, sideB, 255).tex(texMax, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
				}
				
				if (index == 0 && renderBottom){
					val bottomR = color[0]
					val bottomG = color[1]
					val bottomB = color[2]
					
					with(builder){
						pos(mat, maxX, minY, minZ).color(bottomR, bottomG, bottomB, 255).tex(maxU, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
						pos(mat, maxX, minY, maxZ).color(bottomR, bottomG, bottomB, 255).tex(maxU, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
						pos(mat, minX, minY, maxZ).color(bottomR, bottomG, bottomB, 255).tex(minU, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
						pos(mat, minX, minY, minZ).color(bottomR, bottomG, bottomB, 255).tex(minU, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					}
				}
				
				if (index == contents.lastIndex){
					val topR = (color[0] * 1.125F).floorToInt().coerceAtMost(255)
					val topG = (color[1] * 1.125F).floorToInt().coerceAtMost(255)
					val topB = (color[2] * 1.125F).floorToInt().coerceAtMost(255)
					
					with(builder){
						pos(mat, minX, maxY, minZ).color(topR, topG, topB, 255).tex(minU, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
						pos(mat, minX, maxY, maxZ).color(topR, topG, topB, 255).tex(minU, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
						pos(mat, maxX, maxY, maxZ).color(topR, topG, topB, 255).tex(maxU, maxV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
						pos(mat, maxX, maxY, minZ).color(topR, topG, topB, 255).tex(maxU, minV).lightmap(combinedLight).overlay(combinedOverlay).endVertex()
					}
				}
				
				relY += height
			}
		}
	}
	
	override fun render(tile: TileEntityJarODust, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int){
		renderLayers(tile.layers, matrix, buffer, combinedLight, combinedOverlay, renderBottom = false)
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
		
		override fun render(stack: ItemStack, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int){
			val nbt = stack.heeTagOrNull?.getListOfCompounds(TileEntityJarODust.LAYERS_TAG)
			
			if (nbt != null){
				layers.deserializeNBT(nbt)
				renderLayers(layers, matrix, buffer, combinedLight, combinedOverlay, renderBottom = true)
			}
			
			MC.instance.blockRendererDispatcher.blockModelRenderer.renderModelBrightnessColor(matrix.last, buffer.getBuffer(Atlases.getTranslucentCullBlockType()), null, MODEL, 1F, 1F, 1F, combinedLight, combinedOverlay)
		}
	}
}
