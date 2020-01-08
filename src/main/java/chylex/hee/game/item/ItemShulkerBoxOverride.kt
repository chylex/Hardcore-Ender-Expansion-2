package chylex.hee.game.item
import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.game.container.base.IInventoryFromPlayerItem
import chylex.hee.init.ModGuiHandler.GuiType.SHULKER_BOX
import chylex.hee.network.server.PacketServerOpenGui
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.ItemBlock
import chylex.hee.system.migration.vanilla.TextComponentString
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import chylex.hee.system.util.allSlots
import chylex.hee.system.util.find
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
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.inventory.InventoryScreen
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.NonNullList
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.client.event.GuiScreenEvent

class ItemShulkerBoxOverride(block: Block, properties: Properties) : ItemBlock(block, properties){
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
	
	class Inv(override val player: EntityPlayer, private val inventorySlot: Int) : Inventory(BoxSize.LARGE.slots), IInventoryFromPlayerItem{
		// UPDATE name
		
		private val boxStack
			get() = player.inventory.getStack(inventorySlot)
		
		init{
			val boxStack = boxStack
			
			if (isStackValid(boxStack)){
				NonNullList.withSize(size, ItemStack.EMPTY).also {
					ItemStackHelper.loadAllItems(boxStack.nbt.getCompound(TILE_ENTITY_TAG), it)
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
				
				ItemStackHelper.saveAllItems(boxStack.nbt.getCompound(TILE_ENTITY_TAG), it)
			}
			
			return true
		}
	}
	
	// Use handling
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: Hand): ActionResult<ItemStack>{
		val stack = player.getHeldItem(hand)
		val slot = player.inventory.nonEmptySlots.find { it.stack === stack }
		
		if (slot == null){
			return ActionResult(PASS, stack)
		}
		
		SHULKER_BOX.open(player, slot.slot) // TODO it'd be pretty funny if the open animation was shown in inventory/held model but holy shit effort
		return ActionResult(SUCCESS, stack)
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
	object EventHandler{
		@SubscribeEvent(EventPriority.LOWEST)
		fun onMouseInputPre(e: GuiScreenEvent.MouseClickedEvent.Pre){
			val gui = e.gui
			
			if (gui is InventoryScreen && e.button == 1 && !Screen.hasShiftDown()){
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
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		if (MC.currentScreen is InventoryScreen){
			lines.add(TextComponentTranslation("item.hee.shulker_box.tooltip"))
			lines.add(TextComponentString(""))
		}
		
		val contentsTag = stack.nbtOrNull?.getCompoundOrNull(TILE_ENTITY_TAG)
		
		if (contentsTag != null){
			val inventory = NonNullList.withSize(BoxSize.LARGE.slots, ItemStack.EMPTY)
			ItemStackHelper.loadAllItems(contentsTag, inventory)
			
			if (inventory.any { it.isNotEmpty }){
				val counts = Object2IntOpenHashMap<String>()
				
				for(invStack in inventory){
					if (invStack.isNotEmpty){
						counts.addTo(invStack.displayName.string /* UPDATE */, invStack.count)
					}
				}
				
				val sorted = counts.entries.sortedWith(compareBy({ -it.value }, { it.key }))
				
				for((name, count) in sorted.take(TOOLTIP_ENTRY_COUNT)){
					lines.add(TextComponentString("%s x%d".format(name, count)))
				}
				
				if (sorted.size > TOOLTIP_ENTRY_COUNT){
					lines.add(TextComponentString("${TextFormatting.ITALIC}${I18n.format("container.shulkerBox.more", sorted.size - TOOLTIP_ENTRY_COUNT)}"))
				}
			}
		}
		
		if ((lines.lastOrNull() as? TextComponentString)?.text.isNullOrEmpty()){
			lines.removeAt(lines.lastIndex)
		}
	}
}
