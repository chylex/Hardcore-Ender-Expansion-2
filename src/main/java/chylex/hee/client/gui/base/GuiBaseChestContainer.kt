package chylex.hee.client.gui.base
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.size
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.ContainerChest

@Sided(Side.CLIENT)
abstract class GuiBaseChestContainer(container: ContainerChest) : GuiContainer(container){
	private companion object{
		private val TEX_BACKGROUND = Resource.Vanilla("textures/gui/container/generic_54.png")
		private val COLOR_TEXT = RGB(64u).i
	}
	
	private val containerRows = container.lowerChestInventory.size / 9
	
	private val titleContainer = container.lowerChestInventory.displayName.unformattedText
	private val titleInventory = MC.player?.inventory?.displayName?.unformattedText ?: "" // 'mc' not initialized yet
	
	init{
		ySize = 114 + (containerRows * 18)
	}
	
	override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float){
		drawDefaultBackground()
		super.drawScreen(mouseX, mouseY, partialTicks)
		renderHoveredToolTip(mouseX, mouseY)
	}
	
	override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int){
		val x = (width - xSize) / 2
		val y = (height - ySize) / 2
		val heightContainer = 17 + (containerRows * 18)
		
		GL.color(1F, 1F, 1F, 1F)
		mc.textureManager.bindTexture(TEX_BACKGROUND)
		drawTexturedModalRect(x, y, 0, 0, xSize, heightContainer)
		drawTexturedModalRect(x, y + heightContainer, 0, 126, xSize, 96)
	}
	
	override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int){
		fontRenderer.drawString(titleContainer, 8, 6, COLOR_TEXT)
		fontRenderer.drawString(titleInventory, 8, ySize - 94, COLOR_TEXT)
	}
}
