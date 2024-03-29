package chylex.hee.client.gui.screen

import chylex.hee.client.render.util.GL
import chylex.hee.game.Resource
import chylex.hee.game.inventory.util.size
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.container.ChestContainer
import net.minecraft.util.text.ITextComponent

@Sided(Side.CLIENT)
abstract class AbstractChestContainerScreen<T : ChestContainer>(container: T, inventory: PlayerInventory, title: ITextComponent) : ContainerScreen<T>(container, inventory, title) {
	private companion object {
		private val TEX_BACKGROUND = Resource.Vanilla("textures/gui/container/generic_54.png")
		private val COLOR_TEXT = RGB(64u).i
	}
	
	private val containerRows = container.lowerChestInventory.size / 9
	
	init {
		ySize = 114 + (containerRows * 18)
	}
	
	override fun render(matrix: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
		renderBackground(matrix)
		super.render(matrix, mouseX, mouseY, partialTicks)
		renderHoveredTooltip(matrix, mouseX, mouseY)
	}
	
	override fun drawGuiContainerBackgroundLayer(matrix: MatrixStack, partialTicks: Float, mouseX: Int, mouseY: Int) {
		val x = (width - xSize) / 2
		val y = (height - ySize) / 2
		val heightContainer = 17 + (containerRows * 18)
		
		GL.color(1F, 1F, 1F, 1F)
		GL.bindTexture(TEX_BACKGROUND)
		blit(matrix, x, y, 0, 0, xSize, heightContainer)
		blit(matrix, x, y + heightContainer, 0, 126, xSize, 96)
	}
	
	override fun drawGuiContainerForegroundLayer(matrix: MatrixStack, mouseX: Int, mouseY: Int) {
		font.drawText(matrix, title, 8F, 6F, COLOR_TEXT)
		font.drawText(matrix, playerInventory.displayName, 8F, ySize - 94F, COLOR_TEXT)
	}
}
