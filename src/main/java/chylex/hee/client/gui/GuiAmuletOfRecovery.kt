package chylex.hee.client.gui

import chylex.hee.client.gui.base.GuiBaseChestContainer
import chylex.hee.game.container.ContainerAmuletOfRecovery
import chylex.hee.network.server.PacketServerContainerEvent
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.text.ITextComponent
import net.minecraftforge.fml.client.gui.widget.ExtendedButton

@Sided(Side.CLIENT)
class GuiAmuletOfRecovery(container: ContainerAmuletOfRecovery, inventory: PlayerInventory, title: ITextComponent) : GuiBaseChestContainer<ContainerAmuletOfRecovery>(container, inventory, title) {
	override fun init() {
		super.init()
		
		val moveAllTitle = I18n.format("gui.hee.amulet_of_recovery.move_all")
		val moveAllWidth = (font.getStringWidth(moveAllTitle) + 14).coerceAtMost(xSize / 2)
		
		addButton(ExtendedButton(guiLeft + xSize - moveAllWidth - 7, (height / 2) + 6, moveAllWidth, 11, moveAllTitle) {
			PacketServerContainerEvent(0).sendToServer()
		})
	}
}
