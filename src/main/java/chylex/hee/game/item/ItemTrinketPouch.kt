package chylex.hee.game.item
import chylex.hee.client.util.MC
import chylex.hee.game.container.ContainerTrinketPouch
import chylex.hee.game.container.base.IInventoryFromPlayerItem
import chylex.hee.game.container.slot.SlotTrinketItemInventory
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.Infusion.EXPANSION
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.mechanics.trinket.ITrinketHandler
import chylex.hee.game.mechanics.trinket.ITrinketHandlerProvider
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.init.ModGuiHandler.GuiType.TRINKET_POUCH
import chylex.hee.network.server.PacketServerOpenGui
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import chylex.hee.system.util.NBTItemStackList
import chylex.hee.system.util.NBTList.Companion.putList
import chylex.hee.system.util.allSlots
import chylex.hee.system.util.any
import chylex.hee.system.util.find
import chylex.hee.system.util.getListOfItemStacks
import chylex.hee.system.util.getStack
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.setStack
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.inventory.InventoryScreen
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.common.MinecraftForge

class ItemTrinketPouch(properties: Properties) : ItemAbstractTrinket(properties), ITrinketHandlerProvider, IInfusableItem{
	private companion object{
		private const val CONTENTS_TAG = "Contents"
		private const val MOD_COUNTER_TAG = "Version"
		
		private const val INVENTORY_SLOT_TRINKET = Int.MAX_VALUE
		
		private fun isStackValid(stack: ItemStack): Boolean{
			return stack.item is ItemTrinketPouch
		}
		
		private fun getInventoryStack(player: EntityPlayer, slot: Int) = when(slot){
			INVENTORY_SLOT_TRINKET -> TrinketHandler.getTrinketSlotItem(player)
			else -> player.inventory.getStack(slot)
		}
		
		private fun getSlotCount(stack: ItemStack) = when{
			InfusionTag.getList(stack).has(EXPANSION) -> 5
			else -> 3
		}
	}
	
	class Inv(
		override val player: EntityPlayer,
		private val inventorySlot: Int
	) : Inventory(/* UPDATE "gui.hee.trinket_pouch.title", false, */getSlotCount(getInventoryStack(player, inventorySlot))), IInventoryFromPlayerItem, ITrinketHandler{
		private var noLongerValid = false
		private var modCounter = 0
		
		init{
			val pouchItem = getInventoryStack(player, inventorySlot)
			
			if (isStackValid(pouchItem)){
				pouchItem.heeTagOrNull?.let {
					it.getListOfItemStacks(CONTENTS_TAG).forEachIndexed(::setStack)
					modCounter = it.getInt(MOD_COUNTER_TAG)
				}
			}
			else{
				noLongerValid = true
			}
		}
		
		private fun getPouchIfValid(): ItemStack?{
			if (noLongerValid){
				return null
			}
			
			val pouchItem = getInventoryStack(player, inventorySlot).takeIf { isStackValid(it) && it.heeTag.getInt(MOD_COUNTER_TAG) == modCounter }
			
			if (pouchItem == null){
				noLongerValid = true
			}
			
			return pouchItem
		}
		
		override fun tryUpdatePlayerItem(): Boolean{
			return tryUpdatePlayerItem(updateModCounter = false)
		}
		
		private fun tryUpdatePlayerItem(updateModCounter: Boolean): Boolean{
			val pouchItem = getPouchIfValid() ?: return false
			
			var isEmpty = true
			val newList = NBTItemStackList()
			
			for((_, stack) in allSlots){
				newList.append(stack)
				
				if (stack.isNotEmpty){
					isEmpty = false
				}
			}
			
			with(pouchItem.heeTag){
				if (isEmpty){
					remove(CONTENTS_TAG)
				}
				else{
					putList(CONTENTS_TAG, newList)
				}
				
				if (updateModCounter){
					putInt(MOD_COUNTER_TAG, ++modCounter)
				}
			}
			
			return true
		}
		
		// Trinket handler implementation
		
		override fun isInTrinketSlot(stack: ItemStack): Boolean{
			return getPouchIfValid() != null && nonEmptySlots.any { it.stack === stack }
		}
		
		override fun isItemActive(item: ITrinketItem): Boolean{
			return getTrinketIfActive(item) != null
		}
		
		override fun transformIfActive(item: ITrinketItem, transformer: (ItemStack) -> Unit){
			val trinketItem = getTrinketIfActive(item)
			
			if (trinketItem != null){
				transformer(trinketItem)
				tryUpdatePlayerItem(updateModCounter = true)
				
				if (!item.canPlaceIntoTrinketSlot(trinketItem)){
					TrinketHandler.playTrinketBreakFX(player, trinketItem.item)
				}
			}
		}
		
		private fun getTrinketIfActive(item: ITrinketItem): ItemStack?{
			return if (getPouchIfValid() == null)
				null
			else
				return nonEmptySlots.find { (_, stack) -> stack.item === item && item.canPlaceIntoTrinketSlot(stack) }?.stack
		}
	}
	
	// Instance
	
	init{
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun createTrinketHandler(player: EntityPlayer): ITrinketHandler{
		return ((player.openContainer as? ContainerTrinketPouch)?.containerInventory as? Inv) ?: Inv(player, INVENTORY_SLOT_TRINKET) // helpfully updates the opened GUI too
	}
	
	override fun canApplyInfusion(infusion: Infusion): Boolean{
		return ItemAbstractInfusable.onCanApplyInfusion(this, infusion)
	}
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: Hand): ActionResult<ItemStack>{
		val stack = player.getHeldItem(hand)
		val slot = player.inventory.nonEmptySlots.find { it.stack === stack }
		
		if (slot == null){
			return ActionResult(PASS, stack)
		}
		
		TRINKET_POUCH.open(player, slot.slot)
		return ActionResult(SUCCESS, stack)
	}
	
	// Client side
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean{
		return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		super.addInformation(stack, world, lines, flags)
		
		if (MC.currentScreen is InventoryScreen){
			lines.add(TextComponentTranslation("item.hee.trinket_pouch.tooltip"))
		}
		
		ItemAbstractInfusable.onAddInformation(stack, lines)
	}
	
	@Sided(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean{
		return super.hasEffect(stack) || ItemAbstractInfusable.onHasEffect(stack)
	}
	
	@Sided(Side.CLIENT)
	@SubscribeEvent(EventPriority.LOWEST)
	fun onMouseInputPre(e: GuiScreenEvent.MouseClickedEvent.Pre){
		val gui = e.gui
		
		if (gui is InventoryScreen && e.button == 1 && !Screen.hasShiftDown()){
			val hoveredSlot = gui.slotUnderMouse
			
			if (hoveredSlot != null && hoveredSlot.stack.item === this){
				val slotIndex = when(hoveredSlot){
					is SlotTrinketItemInventory -> INVENTORY_SLOT_TRINKET
					else -> hoveredSlot.slotIndex
				}
				
				PacketServerOpenGui(TRINKET_POUCH, slotIndex).sendToServer()
				e.isCanceled = true
			}
		}
	}
}
