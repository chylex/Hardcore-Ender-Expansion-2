package chylex.hee.render
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.mechanics.energy.IEnergyQuantity
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Companion.MAX_POSSIBLE_VALUE
import chylex.hee.init.ModBlocks
import chylex.hee.render.util.RGB
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getTile
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.resources.I18n
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

@EventBusSubscriber(Side.CLIENT, modid = HardcoreEnderExpansion.ID)
object OverlayRenderer{
	private const val BORDER_SIZE  = 4
	private const val LINE_SPACING = 7
	
	private val mc: Minecraft = Minecraft.getMinecraft()
	
	private var clusterLookedAt: TileEntityEnergyCluster? = null
	
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
					"%.2f".format((quantity.floating.value * 100F).floorToInt() * 0.01F)
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
			
			if (pos.getBlock(world) == ModBlocks.ENERGY_CLUSTER){
				clusterLookedAt = pos.getTile(world)
			}
		}
	}
}