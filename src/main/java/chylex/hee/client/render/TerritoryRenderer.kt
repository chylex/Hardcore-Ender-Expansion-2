package chylex.hee.client.render
import chylex.hee.HEE
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.DF_ZERO
import chylex.hee.client.render.util.GL.SF_ONE
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.client.util.MC
import chylex.hee.game.particle.ParticleVoid
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.territory.TerritoryVoid
import chylex.hee.system.Debug
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.lookDirVec
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.posVec
import chylex.hee.system.util.scale
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase
import org.lwjgl.opengl.GL11.GL_GREATER
import kotlin.math.min
import kotlin.math.pow

@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
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
	
	@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
	private object Title{
		private const val FADE_TICKS = 22
		
		private var textTime = 0
		private var textFade = 0
		private var textTitle = ""
		private var textMainColor = RGB(0u)
		private var textShadowColor = RGB(0u)
		
		fun display(newTerritory: TerritoryType){
			val title = I18n.format(newTerritory.translationKey)
			
			textTime = 60 + (2 * title.length)
			textFade = FADE_TICKS
			textTitle = title
			
			with(newTerritory.desc.colors){
				if (tokenTop.asVec.lengthSquared() > tokenBottom.asVec.lengthSquared()){
					textMainColor = tokenTop
					textShadowColor = tokenBottom
				}
				else{
					textMainColor = tokenBottom
					textShadowColor = tokenTop
				}
				
				textShadowColor = textShadowColor.let { RGB(it.red / 2, it.green / 2, it.blue / 2) }
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
		@SubscribeEvent(EventPriority.HIGHEST)
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
			GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA, SF_ONE, DF_ZERO)
			GL.alphaFunc(GL_GREATER, 0F)
			GL.pushMatrix()
			GL.scale(3F, 3F, 3F)
			
			val x = -fontRenderer.getStringWidth(textTitle) * 0.5F
			val y = -fontRenderer.FONT_HEIGHT - 2F
			
			drawTitle(x + 0.5F, y + 0.5F, textShadowColor.withAlpha(opacity.pow(1.25F)))
			drawTitle(x, y, textMainColor.withAlpha(opacity))
			
			GL.popMatrix()
			GL.alphaFunc(GL_GREATER, 0.1F)
			GL.disableBlend()
			GL.popMatrix()
		}
		
		private fun drawTitle(x: Float, y: Float, color: IntColor) = with(MC.fontRenderer){
			resetStyles()
			
			red   = color.red / 255F
			green = color.green / 255F
			blue  = color.blue / 255F
			alpha = color.alpha / 255F
			
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
