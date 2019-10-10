package chylex.hee.game.container.slot
import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.game.container.slot.SlotTrinketItemInventory.Client.isRenderingGUI
import chylex.hee.network.server.PacketServerShiftClickTrinket
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.items.IItemHandler
import org.lwjgl.input.Mouse

class SlotTrinketItemInventory(trinketHandler: IItemHandler, slotNumber: Int) : SlotTrinketItem(trinketHandler, 0, -2000, -2000){
	companion object{
		fun findTrinketSlot(allSlots: List<Slot>): Slot?{
			return allSlots.asReversed().firstOrNull { it is SlotTrinketItemInventory }
		}
		
		fun canShiftClickTrinket(sourceSlot: Slot, trinketSlot: Slot): Boolean{
			return sourceSlot.hasStack && !trinketSlot.hasStack && trinketSlot.isItemValid(sourceSlot.stack)
		}
	}
	
	init{
		this.slotNumber = slotNumber
	}
	
	override fun isHere(inv: IInventory, slot: Int) = true
	
	@Sided(Side.CLIENT)
	override fun isEnabled(): Boolean{
		if (MC.currentScreen !is GuiInventory){
			return false // TODO figure out creative inventory
		}
		
		if (isRenderingGUI){
			isRenderingGUI = false
			
			RenderHelper.disableStandardItemLighting()
			
			MC.textureManager.bindTexture(SlotTrinketItem.Client.TEX_SLOT)
			Gui.drawScaledCustomSizeModalRect(xPos - 1, yPos  - 1, 0F, 0F, 18, 18, 18, 18, SlotTrinketItem.Client.TEX_SLOT_W, SlotTrinketItem.Client.TEX_SLOT_H)
			
			RenderHelper.enableGUIStandardItemLighting()
		}
		
		return true
	}
	
	@Sided(Side.CLIENT)
	@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
	private object Client{
		
		// GUI integration
		
		private val SURVIVAL_INVENTORY_SLOT_POSITIONS = arrayOf(
			Pair(77, 44),
			Pair(77, 26),
			Pair(77,  8)
		)
		
		private val CREATIVE_INVENTORY_SLOT_POSITIONS = arrayOf(
			Pair(127, 20),
			Pair(146, 20),
			Pair(165, 20)
		)
		
		private fun Slot.moveSlotToEmptyPos(allSlots: List<Slot>, positions: Array<Pair<Int, Int>>){
			xPos = -2000
			yPos = -2000
			val (x, y) = positions.firstOrNull { (x, y) -> allSlots.none { it.xPos == x && it.yPos == y } } ?: positions.last()
			xPos = x
			yPos = y
		}
		
		@JvmStatic
		@SubscribeEvent(EventPriority.LOWEST)
		fun onInitGuiPost(e: GuiScreenEvent.InitGuiEvent.Post){
			val gui = e.gui
			
			if (gui is GuiInventory){
				val allSlots = gui.inventorySlots.inventorySlots
				findTrinketSlot(allSlots)?.moveSlotToEmptyPos(allSlots, SURVIVAL_INVENTORY_SLOT_POSITIONS)
			}
		}
		
		@JvmStatic
		@SubscribeEvent(EventPriority.LOWEST)
		fun onMouseInputPre(e: GuiScreenEvent.MouseInputEvent.Pre){
			val gui = e.gui
			
			if (gui is GuiInventory && Mouse.getEventButton() in 0..1 && Mouse.getEventButtonState() && GuiScreen.isShiftKeyDown()){
				val hoveredSlot = gui.slotUnderMouse ?: return
				val trinketSlot = findTrinketSlot(gui.inventorySlots.inventorySlots) ?: return
				
				if (canShiftClickTrinket(hoveredSlot, trinketSlot)){
					PacketServerShiftClickTrinket(hoveredSlot.slotNumber).sendToServer()
					e.isCanceled = true
				}
			}
		}
		
		// Texture rendering
		
		var isRenderingGUI = false
		
		@JvmStatic
		@SubscribeEvent(EventPriority.LOWEST)
		fun onDrawGuiScreenPre(@Suppress("UNUSED_PARAMETER") e: DrawScreenEvent.Pre){
			isRenderingGUI = true
		}
		
		@JvmStatic
		@SubscribeEvent
		fun onDrawGuiScreenPre(@Suppress("UNUSED_PARAMETER") e: DrawScreenEvent.Post){
			isRenderingGUI = false
		}
	}
}
