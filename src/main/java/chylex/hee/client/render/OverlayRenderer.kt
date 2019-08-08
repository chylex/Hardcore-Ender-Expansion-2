package chylex.hee.client.render
import chylex.hee.HEE
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.info.Materials
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.MAX_POSSIBLE_VALUE
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.displayString
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.Resource
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getTile
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
import net.minecraft.client.renderer.GlStateManager.DestFactor.ZERO
import net.minecraft.client.renderer.GlStateManager.FogMode.EXP
import net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE
import net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA
import net.minecraft.client.resources.I18n
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HELMET
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

@EventBusSubscriber(Side.CLIENT, modid = HEE.ID)
object OverlayRenderer{
	private const val BORDER_SIZE  = 4
	private const val LINE_SPACING = 7
	
	private val TEX_ENDER_GOO_OVERLAY = Resource.Custom("textures/overlay/ender_goo.png")
	private val TEX_PURIFIED_ENDER_GOO_OVERLAY = Resource.Custom("textures/overlay/purified_ender_goo.png")
	
	private var clusterLookedAt: TileEntityEnergyCluster? = null
	
	// Ender Goo
	
	@JvmStatic
	@SubscribeEvent
	fun onFogDensity(e: FogDensity){
		val entity = e.entity
		val inside = ActiveRenderInfo.getBlockStateAtEntityViewpoint(entity.world, entity, e.renderPartialTicks.toFloat()).material
		
		if (inside === Materials.ENDER_GOO || inside === Materials.PURIFIED_ENDER_GOO){
			GL.setFog(EXP)
			e.density = if (inside === Materials.ENDER_GOO) 0.66F else 0.06F
			e.isCanceled = true // otherwise the event is ignored
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onRenderHelmetOverlayPre(e: RenderGameOverlayEvent.Pre){
		if (e.type != HELMET){
			return
		}
		
		val player = MC.player ?: return
		val inside = ActiveRenderInfo.getBlockStateAtEntityViewpoint(player.world, player, e.partialTicks).material
		
		if ((inside === Materials.ENDER_GOO || inside === Materials.PURIFIED_ENDER_GOO) && MC.settings.thirdPersonView == 0 && !player.isSpectator){
			val scaledResolution = MC.resolution
			val brightness = player.brightness
			
			GL.color(brightness, brightness, brightness, 1F)
			GL.tryBlendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO)
			
			if (inside === Materials.ENDER_GOO){
				MC.textureManager.bindTexture(TEX_ENDER_GOO_OVERLAY)
			}
			else{
				MC.textureManager.bindTexture(TEX_PURIFIED_ENDER_GOO_OVERLAY)
			}
			
			MC.instance.ingameGUI.drawTexturedModalRect(0, 0, 0, 0, scaledResolution.scaledWidth, scaledResolution.scaledHeight)
			
			GL.color(1F, 1F, 1F, 1F)
		}
	}
	
	// Energy Cluster
	
	@JvmStatic
	@SubscribeEvent
	fun onRenderText(@Suppress("UNUSED_PARAMETER") e: RenderGameOverlayEvent.Text){
		fun drawTextOffScreenCenter(x: Int, y: Int, line: Int, text: String, color: Int){
			val scaledResolution = MC.resolution
			
			with(MC.fontRenderer){
				val centerX = x + (scaledResolution.scaledWidth / 2)
				val centerY = y + (scaledResolution.scaledHeight / 2) + (line * (LINE_SPACING + FONT_HEIGHT))
				
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
			
			val isIgnored = MC.player?.let { player -> ModItems.ENERGY_ORACLE.isClusterIgnored(player, it.pos) } == true
			val firstLine = if (isIgnored) -1 else 0
			
			val health = it.currentHealth
			drawTextOffScreenCenter(0, -40, firstLine, I18n.format("hee.energy.overlay.health", health), health.textColor)
			
			val level = getQuantityString(it.energyLevel)
			val capacity = getQuantityString(it.energyRegenCapacity)
			drawTextOffScreenCenter(0, -40, firstLine + 1, I18n.format("hee.energy.overlay.level", level, capacity), RGB(220u).toInt())
			
			if (isIgnored){
				drawTextOffScreenCenter(0, -40, firstLine + 2, I18n.format("hee.energy.overlay.ignored"), RGB(160u).toInt())
			}
		}
	}
	
	// Block outlines
	
	@JvmStatic
	@SubscribeEvent
	fun onRenderBlockOutline(e: DrawBlockHighlightEvent){
		if (e.target.typeOfHit == BLOCK){ // why the fuck is this still called for air and entities
			val world = e.player.world
			val pos = e.target.blockPos
			val block = pos.getBlock(world)
			
			if (block === ModBlocks.ENERGY_CLUSTER){
				clusterLookedAt = pos.getTile(world)
				e.isCanceled = true
			}
			else if (block is BlockAbstractPortal){
				e.isCanceled = true
			}
		}
	}
}
