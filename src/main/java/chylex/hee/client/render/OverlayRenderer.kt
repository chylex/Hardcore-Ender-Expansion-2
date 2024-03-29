package chylex.hee.client.render

import chylex.hee.HEE
import chylex.hee.client.render.util.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.DF_ZERO
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.SF_ONE
import chylex.hee.client.render.util.SF_SRC_ALPHA
import chylex.hee.client.util.MC
import chylex.hee.game.Resource
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.properties.Materials
import chylex.hee.game.mechanics.energy.IClusterOracleItem
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.MAX_POSSIBLE_VALUE
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.displayString
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModBlocks
import chylex.hee.util.color.IntColor
import chylex.hee.util.color.RGB
import chylex.hee.util.color.RGBA
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import com.mojang.blaze3d.platform.GlStateManager.FogMode.EXP
import net.minecraft.client.gui.AbstractGui
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.client.event.DrawHighlightEvent
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HELMET

@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
object OverlayRenderer {
	private const val BORDER_SIZE = 4
	private const val LINE_SPACING = 7
	
	private val TEX_ENDER_GOO_OVERLAY = Resource.Custom("textures/overlay/ender_goo.png")
	private val TEX_PURIFIED_ENDER_GOO_OVERLAY = Resource.Custom("textures/overlay/purified_ender_goo.png")
	
	private var clusterLookedAt: TileEntityEnergyCluster? = null
	
	// Ender Goo
	
	@SubscribeEvent
	fun onFogDensity(e: FogDensity) {
		val inside = e.info.blockAtCamera.material
		
		if (inside === Materials.ENDER_GOO || inside === Materials.PURIFIED_ENDER_GOO) {
			GL.setFogMode(EXP)
			e.density = if (inside === Materials.ENDER_GOO) 0.66F else 0.06F
			e.isCanceled = true // otherwise the event is ignored
		}
	}
	
	@SubscribeEvent
	fun onRenderHelmetOverlayPre(e: RenderGameOverlayEvent.Pre) {
		if (e.type != HELMET) {
			return
		}
		
		val player = MC.player ?: return
		val inside = MC.gameRenderer.activeRenderInfo.blockAtCamera.material
		
		if ((inside === Materials.ENDER_GOO || inside === Materials.PURIFIED_ENDER_GOO) && MC.settings.pointOfView.func_243192_a() /* RENAME isFirstPerson */ && !player.isSpectator) {
			val window = MC.window
			val brightness = player.brightness
			
			GL.color(brightness, brightness, brightness, 1F)
			GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA, SF_ONE, DF_ZERO)
			
			if (inside === Materials.ENDER_GOO) {
				GL.bindTexture(TEX_ENDER_GOO_OVERLAY)
			}
			else {
				GL.bindTexture(TEX_PURIFIED_ENDER_GOO_OVERLAY)
			}
			
			MC.instance.ingameGUI.blit(e.matrixStack, 0, 0, 0, 0, window.scaledWidth, window.scaledHeight)
			
			GL.color(1F, 1F, 1F, 1F)
		}
	}
	
	// Energy Cluster
	
	@SubscribeEvent
	fun onRenderText(@Suppress("UNUSED_PARAMETER") e: RenderGameOverlayEvent.Text) {
		fun drawTextOffScreenCenter(x: Int, y: Int, line: Int, text: ITextComponent, color: IntColor) {
			val window = MC.window
			
			with(MC.fontRenderer) {
				val centerX = x + (window.scaledWidth / 2)
				val centerY = y + (window.scaledHeight / 2) + (line * (LINE_SPACING + FONT_HEIGHT))
				
				val textWidth = getStringPropertyWidth(text)
				val textHeight = FONT_HEIGHT
				
				val offsetX = -(textWidth / 2)
				val offsetY = -(textHeight / 2)
				
				AbstractGui.fill(e.matrixStack, centerX + offsetX - BORDER_SIZE, centerY + offsetY - BORDER_SIZE, centerX - offsetX + BORDER_SIZE - 1, centerY - offsetY + BORDER_SIZE - 1, RGBA(0u, 0.6F).i)
				drawTextWithShadow(e.matrixStack, text, (centerX + offsetX).toFloat(), (centerY + offsetY).toFloat(), color.i)
			}
		}
		
		clusterLookedAt?.let {
			clusterLookedAt = null
			
			fun getQuantityString(quantity: IEnergyQuantity): String {
				return if (it.energyLevel == MAX_POSSIBLE_VALUE)
					"${TextFormatting.OBFUSCATED}##${TextFormatting.RESET}"
				else
					quantity.displayString
			}
			
			val isIgnored = MC.player?.let { player -> IClusterOracleItem.isPositionIgnored(player, it.pos) } == true
			val firstLine = if (isIgnored) -1 else 0
			
			val health = it.currentHealth
			drawTextOffScreenCenter(0, -40, firstLine, TranslationTextComponent("hee.energy.overlay.health", health), health.textColor)
			
			val level = getQuantityString(it.energyLevel)
			val capacity = getQuantityString(it.energyRegenCapacity)
			drawTextOffScreenCenter(0, -40, firstLine + 1, TranslationTextComponent("hee.energy.overlay.level", level, capacity), RGB(220u))
			
			if (isIgnored) {
				drawTextOffScreenCenter(0, -40, firstLine + 2, TranslationTextComponent("hee.energy.overlay.ignored"), RGB(160u))
			}
		}
	}
	
	// Block outlines
	
	@SubscribeEvent
	fun onRenderBlockOutline(e: DrawHighlightEvent.HighlightBlock) {
		val world = MC.world ?: return
		
		val pos = e.target.pos
		val block = pos.getBlock(world)
		
		if (block === ModBlocks.ENERGY_CLUSTER) {
			clusterLookedAt = pos.getTile(world)
			e.isCanceled = true
		}
		else if (block is BlockAbstractPortal) {
			e.isCanceled = true
		}
	}
}
