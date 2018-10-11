package chylex.hee.game.gui.slot
import chylex.hee.HEE
import chylex.hee.game.gui.slot.SlotTrinketItem.Client.TEX_DEFINITION
import chylex.hee.game.gui.slot.SlotTrinketItem.Client.TEX_SLOT
import chylex.hee.game.gui.slot.SlotTrinketItem.Client.TEX_SLOT_H
import chylex.hee.game.gui.slot.SlotTrinketItem.Client.TEX_SLOT_W
import chylex.hee.game.gui.slot.SlotTrinketItem.Client.isRenderingGUI
import chylex.hee.game.item.base.ITrinketItem
import chylex.hee.system.Resource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.SlotItemHandler

class SlotTrinketItem(trinketHandler: IItemHandler, inventoryIndex: Int) : SlotItemHandler(trinketHandler, 0, -2000, -2000){
	init{
		slotNumber = inventoryIndex
	}
	
	override fun isItemValid(stack: ItemStack): Boolean = (stack.item as? ITrinketItem)?.canPlaceIntoTrinketSlot(stack) == true
	override fun getSlotStackLimit(): Int = 1
	
	override fun isHere(inv: IInventory, slot: Int): Boolean = true
	
	@SideOnly(Side.CLIENT)
	override fun isEnabled(): Boolean{
		val mc = Minecraft.getMinecraft()
		
		if (mc.currentScreen !is GuiInventory){
			return false // TODO figure out creative inventory
		}
		
		if (isRenderingGUI){
			isRenderingGUI = false
			
			RenderHelper.disableStandardItemLighting()
			
			mc.textureManager.bindTexture(TEX_SLOT)
			Gui.drawScaledCustomSizeModalRect(xPos - 1, yPos  - 1, 0F, 0F, 18, 18, 18, 18, TEX_SLOT_W, TEX_SLOT_H)
			
			RenderHelper.enableGUIStandardItemLighting()
		}
		
		return true
	}
	
	@SideOnly(Side.CLIENT) override fun getBackgroundLocation(): ResourceLocation = TEX_SLOT
	@SideOnly(Side.CLIENT) override fun getBackgroundSprite(): TextureAtlasSprite = TEX_DEFINITION
	
	@SideOnly(Side.CLIENT)
	@EventBusSubscriber(Side.CLIENT, modid = HEE.ID)
	private object Client{
		
		// GUI integration
		
		@JvmStatic
		private val SURVIVAL_INVENTORY_SLOT_POSITIONS = arrayOf(
			Pair(77, 44),
			Pair(77, 26),
			Pair(77,  8)
		)
		
		@JvmStatic
		private val CREATIVE_INVENTORY_SLOT_POSITIONS = arrayOf(
			Pair(127, 20),
			Pair(146, 20),
			Pair(165, 20)
		)
		
		@JvmStatic
		private fun Slot.moveSlotToEmptyPos(allSlots: List<Slot>, positions: Array<Pair<Int, Int>>){
			xPos = -2000
			yPos = -2000
			val (x, y) = positions.firstOrNull { (x, y) -> allSlots.none { it.xPos == x && it.yPos == y } } ?: positions.last()
			xPos = x
			yPos = y
		}
		
		@JvmStatic
		@SubscribeEvent(priority = LOWEST)
		fun onInitGuiPost(e: GuiScreenEvent.InitGuiEvent.Post){
			val gui = e.gui
			
			if (gui is GuiInventory){
				val allSlots = gui.inventorySlots.inventorySlots
				val trinketSlot = allSlots.asReversed().firstOrNull { it is SlotTrinketItem }
				
				trinketSlot?.moveSlotToEmptyPos(allSlots, SURVIVAL_INVENTORY_SLOT_POSITIONS)
				// TODO maybe figure out how to support shift+click
			}
		}
		
		// Texture rendering
		
		@JvmField
		val TEX_SLOT = Resource.Custom("textures/gui/trinket.png")
		
		const val TEX_SLOT_W = 64F
		const val TEX_SLOT_H = 32F
		
		@JvmField
		val TEX_DEFINITION = object : TextureAtlasSprite(TEX_SLOT.toString()){
			override fun getMinU(): Float = 19F / TEX_SLOT_W
			override fun getMaxU(): Float = 35F / TEX_SLOT_W
			override fun getMinV(): Float =  1F / TEX_SLOT_H
			override fun getMaxV(): Float = 17F / TEX_SLOT_H
		}
		
		@JvmField
		var isRenderingGUI = false
		
		@JvmStatic
		@SubscribeEvent(priority = LOWEST)
		fun onDrawGuiScreenPre(e: DrawScreenEvent.Pre){
			isRenderingGUI = true
		}
		
		@JvmStatic
		@SubscribeEvent
		fun onDrawGuiScreenPre(e: DrawScreenEvent.Post){
			isRenderingGUI = false
		}
	}
}
