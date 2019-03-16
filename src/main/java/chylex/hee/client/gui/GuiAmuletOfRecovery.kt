package chylex.hee.client.gui
import chylex.hee.client.gui.base.GuiBaseChestContainer
import chylex.hee.game.container.ContainerAmuletOfRecovery
import chylex.hee.network.server.PacketServerContainerEvent
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.client.config.GuiButtonExt
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class GuiAmuletOfRecovery(player: EntityPlayer, itemHeldIn: EnumHand) : GuiBaseChestContainer(ContainerAmuletOfRecovery(player, itemHeldIn)){
	override fun initGui(){
		super.initGui()
		
		val moveAllTitle = I18n.format("gui.hee.amulet_of_recovery.move_all")
		val moveAllWidth = (fontRenderer.getStringWidth(moveAllTitle) + 14).coerceAtMost(xSize / 2)
		
		buttonList.add(GuiButtonExt(1000, guiLeft + xSize - moveAllWidth - 7, (height / 2) + 6, moveAllWidth, 11, moveAllTitle))
	}
	
	override fun actionPerformed(button: GuiButton){
		when(button.id){
			1000 -> PacketServerContainerEvent(0).sendToServer()
			else -> super.actionPerformed(button)
		}
	}
}
