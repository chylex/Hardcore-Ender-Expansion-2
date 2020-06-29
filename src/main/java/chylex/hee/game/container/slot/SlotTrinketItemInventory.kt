package chylex.hee.game.container.slot
import chylex.hee.HEE
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.game.container.slot.SlotTrinketItemInventory.Client.isRenderingGUI
import chylex.hee.network.server.PacketServerShiftClickTrinket
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.facades.Resource
import net.minecraft.client.gui.AbstractGui
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.inventory.InventoryScreen
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.inventory.container.Slot
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.items.IItemHandler

class SlotTrinketItemInventory(trinketHandler: IItemHandler, slotNumber: Int) : SlotTrinketItem(trinketHandler, 0, -2000, -2000){
	companion object{
		private val TEX_SLOT_BACKGROUND = Resource.Custom("textures/gui/slot_background.png")
		
		private const val TEX_SLOT_W = 32
		private const val TEX_SLOT_H = 32
		
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
	
	@Sided(Side.CLIENT)
	override fun isEnabled(): Boolean{
		if (MC.currentScreen !is InventoryScreen){
			return false // TODO figure out creative inventory
		}
		
		if (isRenderingGUI){
			isRenderingGUI = false
			
			RenderHelper.disableStandardItemLighting()
			GL.bindTexture(TEX_SLOT_BACKGROUND)
			AbstractGui.blit(xPos - 1, yPos - 1, 18, 18, 0F, 0F, 18, 18, TEX_SLOT_W, TEX_SLOT_H)
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
		
		@SubscribeEvent(EventPriority.LOWEST)
		fun onInitGuiPost(e: GuiScreenEvent.InitGuiEvent.Post){
			val gui = e.gui
			
			if (gui is InventoryScreen){
				val allSlots = gui.container.inventorySlots
				findTrinketSlot(allSlots)?.moveSlotToEmptyPos(allSlots, SURVIVAL_INVENTORY_SLOT_POSITIONS)
			}
		}
		
		@SubscribeEvent(EventPriority.LOWEST)
		fun onMouseInputPre(e: GuiScreenEvent.MouseClickedEvent.Pre){
			val gui = e.gui
			
			if (gui is InventoryScreen && (e.button == 0 || e.button == 1) && Screen.hasShiftDown()){
				val hoveredSlot = gui.slotUnderMouse ?: return
				val trinketSlot = findTrinketSlot(gui.container.inventorySlots) ?: return
				
				if (canShiftClickTrinket(hoveredSlot, trinketSlot)){
					PacketServerShiftClickTrinket(hoveredSlot.slotNumber).sendToServer()
					e.isCanceled = true
				}
			}
		}
		
		// Texture rendering
		
		var isRenderingGUI = false
		
		@SubscribeEvent(EventPriority.LOWEST)
		fun onDrawGuiScreenPre(@Suppress("UNUSED_PARAMETER") e: DrawScreenEvent.Pre){
			isRenderingGUI = true
		}
		
		@SubscribeEvent
		fun onDrawGuiScreenPre(@Suppress("UNUSED_PARAMETER") e: DrawScreenEvent.Post){
			isRenderingGUI = false
		}
	}
}
