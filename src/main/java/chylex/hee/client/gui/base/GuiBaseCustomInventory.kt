package chylex.hee.client.gui.base
import chylex.hee.client.render.util.GL
import chylex.hee.game.container.base.ContainerBaseCustomInventory
import chylex.hee.system.util.color.RGB
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.resources.I18n
import net.minecraft.inventory.IInventory
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
abstract class GuiBaseCustomInventory<T : IInventory>(container: ContainerBaseCustomInventory<T>) : GuiContainer(container){
	private companion object{
		private val COLOR_TEXT = RGB(64u).toInt()
	}
	
	protected abstract val texBackground: ResourceLocation
	protected abstract val titleContainer: String
	
	private val titleInventory = Minecraft.getMinecraft().player.inventory.displayName.unformattedText // 'mc' not initialized yet
	
	override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float){
		drawDefaultBackground()
		super.drawScreen(mouseX, mouseY, partialTicks)
		renderHoveredToolTip(mouseX, mouseY)
	}
	
	override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int){
		val x = (width - xSize) / 2
		val y = (height - ySize) / 2
		
		GL.color(1F, 1F, 1F, 1F)
		mc.textureManager.bindTexture(texBackground)
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize)
	}
	
	override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int){
		fontRenderer.drawString(I18n.format(titleContainer), 8, 6, COLOR_TEXT)
		fontRenderer.drawString(titleInventory, 8, ySize - 94, COLOR_TEXT)
	}
}
