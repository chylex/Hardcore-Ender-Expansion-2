package chylex.hee.client.render
import chylex.hee.HEE
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.game.particle.ParticleVoid
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.territory.TerritoryVoid
import chylex.hee.system.Debug
import chylex.hee.system.util.color.IColor
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.lookDirVec
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.posVec
import chylex.hee.system.util.scale
import net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
import net.minecraft.client.renderer.GlStateManager.DestFactor.ZERO
import net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE
import net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase
import net.minecraftforge.fml.relauncher.Side
import org.lwjgl.opengl.GL11.GL_GREATER
import kotlin.math.min
import kotlin.math.pow

@EventBusSubscriber(Side.CLIENT, modid = HEE.ID)
object TerritoryRenderer{
	private var prevChunkX = Int.MAX_VALUE
	private var prevTerritory: TerritoryType? = null
	
	@JvmStatic
	@SubscribeEvent
	fun onClientTick(e: ClientTickEvent){
		if (e.phase == Phase.START){
			val player = MC.player
			
			if (player != null && player.world.provider is WorldProviderEndCustom && player.ticksExisted > 0){
				Void.tick(player)
				Title.tick()
				
				val newChunkX = player.chunkCoordX
				
				if (prevChunkX != newChunkX){
					prevChunkX = newChunkX
					
					val newTerritory = TerritoryType.fromX(player.posX.floorToInt())
					
					if (prevTerritory != newTerritory){
						prevTerritory = newTerritory
						
						if (newTerritory != null){
							Void.reset()
							Title.display(newTerritory)
							newTerritory.desc.environment.setupClient()
						}
					}
				}
				
				prevTerritory?.desc?.environment?.tickClient()
			}
			else if (prevTerritory != null){
				prevTerritory = null
				prevChunkX = Int.MAX_VALUE
				
				Void.reset()
				Title.reset()
			}
		}
	}
	
	// Void handling
	
	val VOID_FACTOR_VALUE
		get() = Void.voidFactor.get(MC.partialTicks)
	
	private object Void{
		private val VOID_PARTICLE = ParticleSpawnerCustom(
			type = ParticleVoid,
			pos = InBox(8F)
		)
		
		val voidFactor = LerpedFloat(TerritoryVoid.OUTSIDE_VOID_FACTOR)
		
		init{
			if (Debug.enabled){
				MinecraftForge.EVENT_BUS.register(this)
			}
		}
		
		fun tick(player: EntityPlayer){
			val factor = TerritoryVoid.getVoidFactor(player).also(voidFactor::update)
			
			if (factor == TerritoryVoid.OUTSIDE_VOID_FACTOR || MC.instance.isGamePaused){
				return
			}
			
			if (factor > -1F){
				val rand = player.rng
				
				val mp = min(1F, (factor * 0.275F) + 0.275F)
				val extra = (mp.pow(1.5F) * 12F).floorToInt()
				
				VOID_PARTICLE.spawn(Point(player, heightMp = 0.5F, amount = 2 + extra), rand)
				VOID_PARTICLE.spawn(Point(player.posVec.add(player.lookDirVec.scale(5)), amount = extra / 2), rand)
			}
		}
		
		fun reset(){
			voidFactor.updateImmediately(TerritoryVoid.OUTSIDE_VOID_FACTOR)
		}
		
		@SubscribeEvent
		fun onRenderGameOverlayText(e: RenderGameOverlayEvent.Text){
			if (MC.settings.showDebugInfo && MC.player?.dimension == 1){
				with(e.left){
					add("")
					add("End Void Factor: ${"%.3f".format(voidFactor.currentValue)}")
				}
			}
		}
	}
	
	// Text rendering
	
	@EventBusSubscriber(Side.CLIENT, modid = HEE.ID)
	private object Title{
		private const val FADE_TICKS = 22
		
		private var textTime = 0
		private var textFade = 0
		private var textTitle = ""
		private var textMainColor: IColor = RGB(0u)
		private var textShadowColor: IColor = RGB(0u)
		
		fun display(newTerritory: TerritoryType){
			val title = I18n.format(newTerritory.translationKey)
			
			textTime = 60 + (2 * title.length)
			textFade = FADE_TICKS
			textTitle = title
			
			with(newTerritory.desc.colors){
				if (tokenTop.toVec().lengthSquared() > tokenBottom.toVec().lengthSquared()){
					textMainColor = tokenTop
					textShadowColor = tokenBottom
				}
				else{
					textMainColor = tokenBottom
					textShadowColor = tokenTop
				}
				
				textShadowColor = textShadowColor.toRGB().let { RGB(it.red / 2, it.green / 2, it.blue / 2) }
			}
		}
		
		fun tick(){
			if (textTime > 0){
				--textTime
				--textFade
			}
		}
		
		fun reset(){
			textTime = 0
		}
		
		@JvmStatic
		@SubscribeEvent(priority = HIGHEST)
		fun onRenderGameOverlayText(e: RenderGameOverlayEvent.Text){
			if (textTime == 0){
				return
			}
			
			val fontRenderer = MC.fontRenderer
			val resolution = e.resolution
			val width = resolution.scaledWidth
			val height = resolution.scaledHeight
			
			val opacity = when{
				textFade > 0          -> ((FADE_TICKS - (textFade - e.partialTicks)) / FADE_TICKS.toFloat()).coerceIn(0F, 1F)
				textTime < FADE_TICKS -> ((textTime - e.partialTicks) / FADE_TICKS.toFloat()).coerceIn(0F, 1F)
				else                  -> 1F
			}
			
			GL.pushMatrix()
			GL.translate(width * 0.5F, height * 0.5F, 0F)
			GL.enableBlend()
			GL.tryBlendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO)
			GL.alphaFunc(GL_GREATER, 0F)
			GL.pushMatrix()
			GL.scale(3F, 3F, 3F)
			
			val x = -fontRenderer.getStringWidth(textTitle) * 0.5F
			val y = -fontRenderer.FONT_HEIGHT - 2F
			
			drawTitle(x + 0.5F, y + 0.5F, textShadowColor.toInt(opacity.pow(1.25F)))
			drawTitle(x, y, textMainColor.toInt(opacity))
			
			GL.popMatrix()
			GL.alphaFunc(GL_GREATER, 0.1F)
			GL.disableBlend()
			GL.popMatrix()
		}
		
		private fun drawTitle(x: Float, y: Float, color: Int) = with(MC.fontRenderer){
			resetStyles()
			
			alpha = ((color shr 24) and 255) / 255.0F
			red   = ((color shr 16) and 255) / 255.0F
			green = ((color shr  8) and 255) / 255.0F
			blue  = (color and 255) / 255.0F
			
			posX = x
			posY = y
			
			val text = if (bidiFlag)
				bidiReorder(textTitle)
			else
				textTitle
			
			GL.color(red, green, blue, alpha)
			renderStringAtPos(text, false)
		}
	}
}
