package chylex.hee.game.render
import chylex.hee.HEE
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.material.Materials
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.MAX_POSSIBLE_VALUE
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.displayString
import chylex.hee.game.render.util.GL
import chylex.hee.game.render.util.RGB
import chylex.hee.init.ModBlocks
import chylex.hee.system.Resource
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getTile
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
import net.minecraft.client.renderer.GlStateManager.DestFactor.ZERO
import net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE
import net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA
import net.minecraft.client.resources.I18n
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HELMET
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

@EventBusSubscriber(Side.CLIENT, modid = HEE.ID)
object OverlayRenderer{
	private const val BORDER_SIZE  = 4
	private const val LINE_SPACING = 7
	
	@JvmStatic private val TEX_ENDER_GOO_OVERLAY = Resource.Custom("textures/overlay/ender_goo.png")
	
	private val mc = Minecraft.getMinecraft()
	
	private var clusterLookedAt: TileEntityEnergyCluster? = null
	
	// Ender Goo
	
	@JvmStatic
	@SubscribeEvent
	fun onRenderHelmetOverlayPre(e: RenderGameOverlayEvent.Pre){
		if (e.type != HELMET){
			return
		}
		
		val player = mc.player
		val insideOf = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.world, player, e.partialTicks)
		
		if (insideOf.material === Materials.ENDER_GOO && mc.gameSettings.thirdPersonView == 0 && !player.isSpectator){
			val scaledResolution = ScaledResolution(mc)
			val brightness = player.brightness
			
			GL.color(brightness, brightness, brightness, 1F)
			GL.tryBlendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO)
			
			mc.textureManager.bindTexture(TEX_ENDER_GOO_OVERLAY)
			mc.ingameGUI.drawTexturedModalRect(0, 0, 0, 0, scaledResolution.scaledWidth, scaledResolution.scaledHeight)
			
			GL.color(1F, 1F, 1F, 1F)
		}
	}
	
	// Energy Cluster
	
	@JvmStatic
	@SubscribeEvent
	fun onRenderText(e: RenderGameOverlayEvent.Text){
		fun drawTextOffScreenCenter(x: Int, y: Int, text: String, color: Int){
			val scaledResolution = ScaledResolution(mc)
			val centerX = x + (scaledResolution.scaledWidth / 2)
			val centerY = y + (scaledResolution.scaledHeight / 2)
			
			with(mc.fontRenderer){
				val textWidth = getStringWidth(text)
				val textHeight = FONT_HEIGHT
				
				val offsetX = -(textWidth / 2)
				val offsetY = -(textHeight / 2)
				
				Gui.drawRect(centerX + offsetX - BORDER_SIZE, centerY + offsetY - BORDER_SIZE, centerX - offsetX + BORDER_SIZE - 1, centerY - offsetY + BORDER_SIZE - 1, RGB(0, 0, 0).toInt(0.6F))
				drawStringWithShadow(text, (centerX + offsetX).toFloat(), (centerY + offsetY).toFloat(), color)
			}
		}
		
		clusterLookedAt?.let {
			clusterLookedAt = null
			
			fun getQuantityString(quantity: IEnergyQuantity): String{
				return if (it.energyLevel == MAX_POSSIBLE_VALUE)
					"${TextFormatting.OBFUSCATED}##${TextFormatting.RESET}"
				else
					quantity.displayString
			}
			
			val health = it.currentHealth
			drawTextOffScreenCenter(0, -40, I18n.format("hee.energy.overlay.health", health), health.textColor)
			
			val level = getQuantityString(it.energyLevel)
			val capacity = getQuantityString(it.energyRegenCapacity)
			drawTextOffScreenCenter(0, -40 + LINE_SPACING + mc.fontRenderer.FONT_HEIGHT, I18n.format("hee.energy.overlay.level", level, capacity), RGB(220).toInt())
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onRenderBlockOutline(e: DrawBlockHighlightEvent){
		if (e.target.typeOfHit == BLOCK){ // why the fuck is this still called for air and entities
			val world = e.player.world
			val pos = e.target.blockPos
			
			if (pos.getBlock(world) === ModBlocks.ENERGY_CLUSTER){
				clusterLookedAt = pos.getTile(world)
				e.isCanceled = true
			}
		}
	}
}
