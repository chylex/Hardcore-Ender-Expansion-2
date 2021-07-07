package chylex.hee.client.gui.screen

import chylex.hee.game.Resource
import chylex.hee.game.container.AbstractCustomInventoryContainer
import chylex.hee.game.container.ContainerTrinketPouch
import chylex.hee.game.inventory.util.size
import chylex.hee.util.color.RGBA
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.text.ITextComponent

@Sided(Side.CLIENT)
class GuiTrinketPouch(container: ContainerTrinketPouch, inventory: PlayerInventory, title: ITextComponent) : AbstractCustomInventoryScreen<ContainerTrinketPouch>(container, inventory, title) {
	override val texBackground = Resource.Custom("textures/gui/trinket_pouch.png")
	override val titleContainer = "gui.hee.trinket_pouch.title"
	
	private val hiddenSlots: Int
	private val hiddenSlotColor = RGBA(0u, 0.25F).i
	
	init {
		ySize = ContainerTrinketPouch.HEIGHT
		hiddenSlots = ContainerTrinketPouch.MAX_SLOTS - (container as AbstractCustomInventoryContainer<*>).containerInventory.size
	}
	
	override fun drawGuiContainerBackgroundLayer(matrix: MatrixStack, partialTicks: Float, mouseX: Int, mouseY: Int) {
		super.drawGuiContainerBackgroundLayer(matrix, partialTicks, mouseX, mouseY)
		
		val middleSlot = ContainerTrinketPouch.MAX_SLOTS / 2
		
		repeat(hiddenSlots) {
			renderSlotCover(matrix, middleSlot + ((ContainerTrinketPouch.MAX_SLOTS - it) / 2) * (if (it % 2 == 0) -1 else 1))
		}
	}
	
	private fun renderSlotCover(matrix: MatrixStack, index: Int) {
		val x = guiLeft + 44 + (index * 18)
		val y = guiTop + 18
		fill(matrix, x, y, x + 16, y + 16, hiddenSlotColor)
	}
}
