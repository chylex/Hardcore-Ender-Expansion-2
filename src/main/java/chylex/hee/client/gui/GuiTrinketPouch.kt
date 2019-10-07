package chylex.hee.client.gui
import chylex.hee.client.gui.base.GuiBaseCustomInventory
import chylex.hee.game.container.ContainerTrinketPouch
import chylex.hee.game.container.base.ContainerBaseCustomInventory
import chylex.hee.game.item.ItemTrinketPouch.Inventory
import chylex.hee.system.Resource
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IntColor.Companion.RGBA
import chylex.hee.system.util.size
import net.minecraft.client.gui.Gui
import net.minecraft.entity.player.EntityPlayer

@Sided(Side.CLIENT)
class GuiTrinketPouch(player: EntityPlayer, inventorySlot: Int) : GuiBaseCustomInventory<Inventory>(ContainerTrinketPouch(player, inventorySlot)){
	override val texBackground = Resource.Custom("textures/gui/trinket_pouch.png")
	override val titleContainer = "gui.hee.trinket_pouch.title"
	
	private val hiddenSlots: Int
	private val hiddenSlotColor = RGBA(0u, 0.25F).i
	
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
