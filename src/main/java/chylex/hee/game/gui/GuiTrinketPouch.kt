package chylex.hee.game.gui
import chylex.hee.game.gui.base.ContainerBaseCustomInventory
import chylex.hee.game.gui.base.GuiBaseCustomInventory
import chylex.hee.game.item.ItemTrinketPouch
import chylex.hee.system.Resource
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.size
import net.minecraft.client.gui.Gui
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class GuiTrinketPouch(player: EntityPlayer, inventorySlot: Int) : GuiBaseCustomInventory<ItemTrinketPouch.Inventory>(ContainerTrinketPouch(player, inventorySlot)){
	override val texBackground = Resource.Custom("textures/gui/trinket_pouch.png")
	override val titleContainer = "gui.hee.trinket_pouch.title"
	
	private val hiddenSlots: Int
	private val hiddenSlotColor = RGB(0u).toInt(0.25F)
	
	init{
		ySize = ContainerTrinketPouch.HEIGHT
		hiddenSlots = ContainerTrinketPouch.MAX_SLOTS - (inventorySlots as ContainerBaseCustomInventory<*>).containerInventory.size
	}
	
	override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int){
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
		
		val middleSlot = ContainerTrinketPouch.MAX_SLOTS / 2
		
		repeat(hiddenSlots){
			renderSlotCover(middleSlot + ((ContainerTrinketPouch.MAX_SLOTS - it) / 2) * (if (it % 2 == 0) -1 else 1))
		}
	}
	
	private fun renderSlotCover(index: Int){
		val x = guiLeft + 44 + (index * 18)
		val y = guiTop + 18
		Gui.drawRect(x, y, x + 16, y + 16, hiddenSlotColor)
	}
}
