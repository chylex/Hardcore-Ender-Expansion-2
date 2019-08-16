package chylex.hee.game.item
import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.game.container.base.IInventoryFromPlayerItem
import chylex.hee.init.ModGuiHandler.GuiType.SHULKER_BOX
import chylex.hee.network.server.PacketServerOpenGui
import chylex.hee.system.util.allSlots
import chylex.hee.system.util.getCompoundOrNull
import chylex.hee.system.util.getStack
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.nbt
import chylex.hee.system.util.nbtOrNull
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.setStack
import chylex.hee.system.util.size
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.InventoryBasic
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.item.ItemShulkerBox
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult.PASS
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumHand
import net.minecraft.util.NonNullList
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.input.Mouse

class ItemShulkerBoxOverride(block: Block) : ItemShulkerBox(block){
	private companion object{
		private const val TILE_ENTITY_TAG = "BlockEntityTag"
		private const val TOOLTIP_ENTRY_COUNT = 5
		
		private fun isStackValid(stack: ItemStack): Boolean{
			return stack.item is ItemShulkerBoxOverride
		}
	}
	
	enum class BoxSize(val slots: Int){
		LARGE(27)
	}
	
	class Inventory(override val player: EntityPlayer, private val inventorySlot: Int) : InventoryBasic("", false, BoxSize.LARGE.slots), IInventoryFromPlayerItem{
		private val boxStack
			get() = player.inventory.getStack(inventorySlot)
		
		init{
			val boxStack = boxStack
			
			if (isStackValid(boxStack)){
				NonNullList.withSize(size, ItemStack.EMPTY).also {
					ItemStackHelper.loadAllItems(boxStack.nbt.getCompoundTag(TILE_ENTITY_TAG), it)
					it.forEachIndexed(::setStack)
				}
			}
		}
		
		override fun tryUpdatePlayerItem(): Boolean{
			val boxStack = boxStack
			
			if (!isStackValid(boxStack)){
				return false
			}
			
			NonNullList.withSize(size, ItemStack.EMPTY).also {
				for((slot, stack) in allSlots){
					it[slot] = stack
				}
				
				ItemStackHelper.saveAllItems(boxStack.nbt.getCompoundTag(TILE_ENTITY_TAG), it)
			}
			
			return true
		}
		
		override fun getName(): String{
			return boxStack.let { if (it.hasDisplayName()) it.displayName else "${it.translationKey}.name" }
		}
		
		override fun hasCustomName(): Boolean{
			return boxStack.hasDisplayName()
		}
	}
	
	// Use handling
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack>{
		val stack = player.getHeldItem(hand)
		val slot = player.inventory.nonEmptySlots.asSequence().find { it.stack === stack }
		
		if (slot == null){
			return ActionResult(PASS, stack)
		}
		
		SHULKER_BOX.open(player, slot.slot) // TODO it'd be pretty funny if the open animation was shown in inventory/held model but holy shit effort
		return ActionResult(SUCCESS, stack)
	}
	
	// Client side
	
	@SideOnly(Side.CLIENT)
	@EventBusSubscriber(Side.CLIENT, modid = HEE.ID)
	object EventHandler{
		@JvmStatic
		@SubscribeEvent(priority = LOWEST)
		fun onMouseInputPre(e: GuiScreenEvent.MouseInputEvent.Pre){
			val gui = e.gui
			
			if (gui is GuiInventory && Mouse.getEventButton() == 1 && Mouse.getEventButtonState() && !GuiScreen.isShiftKeyDown()){
				val hoveredSlot = gui.slotUnderMouse
				
				if (hoveredSlot != null && isStackValid(hoveredSlot.stack)){
					PacketServerOpenGui(SHULKER_BOX, hoveredSlot.slotIndex).sendToServer()
					e.isCanceled = true
				}
			}
		}
	}
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean{
		return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		if (MC.currentScreen is GuiInventory){
			lines.add(I18n.format("item.hee.shulker_box.tooltip"))
			lines.add("")
		}
		
		val contentsTag = stack.nbtOrNull?.getCompoundOrNull(TILE_ENTITY_TAG)
		
		if (contentsTag != null){
			val inventory = NonNullList.withSize(BoxSize.LARGE.slots, ItemStack.EMPTY)
			ItemStackHelper.loadAllItems(contentsTag, inventory)
			
			if (inventory.any { it.isNotEmpty }){
				val counts = Object2IntOpenHashMap<String>()
				
				for(invStack in inventory){
					if (invStack.isNotEmpty){
						counts.addTo(invStack.displayName, invStack.count)
					}
				}
				
				val sorted = counts.entries.sortedWith(compareBy({ -it.value }, { it.key }))
				
				for((name, count) in sorted.take(TOOLTIP_ENTRY_COUNT)){
					lines.add("%s x%d".format(name, count))
				}
				
				if (sorted.size > TOOLTIP_ENTRY_COUNT){
					lines.add("${TextFormatting.ITALIC}${I18n.format("container.shulkerBox.more", sorted.size - TOOLTIP_ENTRY_COUNT)}")
				}
			}
		}
		
		if (lines.lastOrNull()?.isEmpty() == true){
			lines.removeAt(lines.lastIndex)
		}
	}
}
