package chylex.hee.client.gui.base
import chylex.hee.client.render.util.GL
import chylex.hee.game.container.base.ContainerBaseCustomInventory
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IntColor.Companion.RGB
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent

@Sided(Side.CLIENT)
abstract class GuiBaseCustomInventory<T : ContainerBaseCustomInventory<*>>(container: T, inventory: PlayerInventory, title: ITextComponent) : ContainerScreen<T>(container, inventory, title){
	private companion object{
		private val COLOR_TEXT = RGB(64u).i
	}
	
	protected abstract val texBackground: ResourceLocation
	protected abstract val titleContainer: String
	
	override fun render(mouseX: Int, mouseY: Int, partialTicks: Float){
		renderBackground()
		super.render(mouseX, mouseY, partialTicks)
		renderHoveredToolTip(mouseX, mouseY)
	}
	
	override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int){
		val x = (width - xSize) / 2
		val y = (height - ySize) / 2
		
		GL.color(1F, 1F, 1F, 1F)
		GL.bindTexture(texBackground)
		blit(x, y, 0, 0, xSize, ySize)
	}
	
	override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int){
		font.drawString(title.formattedText, 8F, 6F, COLOR_TEXT)
		font.drawString(playerInventory.displayName.formattedText, 8F, ySize - 94F, COLOR_TEXT)
	}
}
