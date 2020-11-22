package chylex.hee.client.gui.base
import chylex.hee.client.render.gl.GL
import chylex.hee.game.container.base.ContainerBaseCustomInventory
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
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
	
	override fun render(matrix: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float){
		renderBackground(matrix)
		super.render(matrix, mouseX, mouseY, partialTicks)
		renderHoveredTooltip(matrix, mouseX, mouseY)
	}
	
	override fun drawGuiContainerBackgroundLayer(matrix: MatrixStack, partialTicks: Float, mouseX: Int, mouseY: Int){
		val x = (width - xSize) / 2
		val y = (height - ySize) / 2
		
		GL.color(1F, 1F, 1F, 1F)
		GL.bindTexture(texBackground)
		blit(matrix, x, y, 0, 0, xSize, ySize)
	}
	
	override fun drawGuiContainerForegroundLayer(matrix: MatrixStack, mouseX: Int, mouseY: Int){
		font.drawString(matrix, title.string, 8F, 6F, COLOR_TEXT)
		font.drawString(matrix, playerInventory.displayName.string, 8F, ySize - 94F, COLOR_TEXT)
	}
}
