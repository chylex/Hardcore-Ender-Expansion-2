package chylex.hee.client.gui.screen

import chylex.hee.game.container.ContainerAmuletOfRecovery
import chylex.hee.network.server.PacketServerContainerEvent
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.client.gui.widget.ExtendedButton

@Sided(Side.CLIENT)
class GuiAmuletOfRecovery(container: ContainerAmuletOfRecovery, inventory: PlayerInventory, title: ITextComponent) : AbstractChestContainerScreen<ContainerAmuletOfRecovery>(container, inventory, title) {
	override fun init() {
		super.init()
		
		val moveAllTitle = TranslationTextComponent("gui.hee.amulet_of_recovery.move_all")
		val moveAllWidth = (font.getStringPropertyWidth(moveAllTitle) + 14).coerceAtMost(xSize / 2)
		
		addButton(ExtendedButton(guiLeft + xSize - moveAllWidth - 7, (height / 2) + 6, moveAllWidth, 11, moveAllTitle) {
			PacketServerContainerEvent(0).sendToServer()
		})
	}
}
