package chylex.hee.game.item

import chylex.hee.client.util.MC
import chylex.hee.game.container.ContainerTrinketPouch
import chylex.hee.game.container.slot.SlotTrinketItemInventory
import chylex.hee.game.inventory.util.IInventoryFromPlayerItem
import chylex.hee.game.inventory.util.allSlots
import chylex.hee.game.inventory.util.getStack
import chylex.hee.game.inventory.util.nonEmptySlots
import chylex.hee.game.inventory.util.setStack
import chylex.hee.game.item.infusion.IInfusableItem
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.Infusion.EXPANSION
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.item.util.isNotEmpty
import chylex.hee.game.mechanics.trinket.ITrinketHandler
import chylex.hee.game.mechanics.trinket.ITrinketHandlerProvider
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.init.ModContainers
import chylex.hee.network.server.PacketServerOpenInventoryItem
import chylex.hee.system.MinecraftForgeEventBus
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.collection.any
import chylex.hee.util.collection.find
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.nbt.NBTItemStackList
import chylex.hee.util.nbt.getListOfItemStacks
import chylex.hee.util.nbt.putList
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.inventory.InventoryScreen
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraftforge.client.event.GuiScreenEvent

class ItemTrinketPouch(properties: Properties) : ItemAbstractTrinket(properties), ITrinketHandlerProvider, IInfusableItem {
	private companion object {
		private const val CONTENTS_TAG = "Contents"
		private const val MOD_COUNTER_TAG = "Version"
		
		private fun isStackValid(stack: ItemStack): Boolean {
			return stack.item is ItemTrinketPouch
		}
		
		private fun getInventoryStack(player: PlayerEntity, slot: Int) = when (slot) {
			SlotTrinketItemInventory.INTERNAL_INDEX -> TrinketHandler.getTrinketSlotItem(player)
			else                                    -> player.inventory.getStack(slot)
		}
		
		private fun getSlotCount(stack: ItemStack) = when {
			InfusionTag.getList(stack).has(EXPANSION) -> 5
			else                                      -> 3
		}
	}
	
	class ContainerProvider(private val stack: ItemStack, private val slot: Int) : INamedContainerProvider {
		override fun getDisplayName(): ITextComponent {
			return stack.displayName
		}
		
		override fun createMenu(id: Int, inventory: PlayerInventory, player: PlayerEntity): Container {
			return ContainerTrinketPouch(id, player, slot)
		}
	}
	
	class Inv(
		override val player: PlayerEntity,
		private val inventorySlot: Int,
	) : Inventory(getSlotCount(getInventoryStack(player, inventorySlot))), IInventoryFromPlayerItem, ITrinketHandler {
		private var noLongerValid = false
		private var modCounter = 0
		
		init {
			val pouchItem = getInventoryStack(player, inventorySlot)
			
			if (isStackValid(pouchItem)) {
				pouchItem.heeTagOrNull?.let {
					it.getListOfItemStacks(CONTENTS_TAG).forEachIndexed(::setStack)
					modCounter = it.getInt(MOD_COUNTER_TAG)
				}
			}
			else {
				noLongerValid = true
			}
		}
		
		private fun getPouchIfValid(): ItemStack? {
			if (noLongerValid) {
				return null
			}
			
			val pouchItem = getInventoryStack(player, inventorySlot).takeIf { isStackValid(it) && it.heeTag.getInt(MOD_COUNTER_TAG) == modCounter }
			
			if (pouchItem == null) {
				noLongerValid = true
			}
			
			return pouchItem
		}
		
		override fun tryUpdatePlayerItem(): Boolean {
			return tryUpdatePlayerItem(updateModCounter = false)
		}
		
		private fun tryUpdatePlayerItem(updateModCounter: Boolean): Boolean {
			val pouchItem = getPouchIfValid() ?: return false
			
			var isEmpty = true
			val newList = NBTItemStackList()
			
			for ((_, stack) in allSlots) {
				newList.append(stack)
				
				if (stack.isNotEmpty) {
					isEmpty = false
				}
			}
			
			with(pouchItem.heeTag) {
				if (isEmpty) {
					remove(CONTENTS_TAG)
				}
				else {
					putList(CONTENTS_TAG, newList)
				}
				
				if (updateModCounter) {
					putInt(MOD_COUNTER_TAG, ++modCounter)
				}
			}
			
			return true
		}
		
		// Trinket handler implementation
		
		override fun isInTrinketSlot(stack: ItemStack): Boolean {
			return getPouchIfValid() != null && nonEmptySlots.any { it.stack === stack }
		}
		
		override fun isItemActive(item: ITrinketItem): Boolean {
			return getTrinketIfActive(item) != null
		}
		
		override fun transformIfActive(item: ITrinketItem, transformer: (ItemStack) -> Unit) {
			val trinketItem = getTrinketIfActive(item)
			
			if (trinketItem != null) {
				transformer(trinketItem)
				tryUpdatePlayerItem(updateModCounter = true)
				
				if (!item.canPlaceIntoTrinketSlot(trinketItem)) {
					TrinketHandler.playTrinketBreakFX(player, trinketItem.item)
				}
			}
		}
		
		private fun getTrinketIfActive(item: ITrinketItem): ItemStack? {
			return if (getPouchIfValid() == null)
				null
			else
				return nonEmptySlots.find { (_, stack) -> stack.item === item && item.canPlaceIntoTrinketSlot(stack) }?.stack
		}
	}
	
	// Instance
	
	init {
		MinecraftForgeEventBus.register(this)
	}
	
	override fun createTrinketHandler(player: PlayerEntity): ITrinketHandler {
		return (player.openContainer as? ContainerTrinketPouch)?.containerInventory ?: Inv(player, SlotTrinketItemInventory.INTERNAL_INDEX) // helpfully updates the opened GUI too
	}
	
	override fun canApplyInfusion(infusion: Infusion): Boolean {
		return ItemAbstractInfusable.onCanApplyInfusion(this, infusion)
	}
	
	override fun onItemRightClick(world: World, player: PlayerEntity, hand: Hand): ActionResult<ItemStack> {
		val stack = player.getHeldItem(hand)
		val slot = player.inventory.nonEmptySlots.find { it.stack === stack }
		
		if (slot == null) {
			return ActionResult(PASS, stack)
		}
		
		ModContainers.open(player, ContainerProvider(stack, slot.slot), slot.slot)
		return ActionResult(SUCCESS, stack)
	}
	
	// Client side
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean {
		return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag) {
		super.addInformation(stack, world, lines, flags)
		
		if (MC.currentScreen is InventoryScreen) {
			lines.add(TranslationTextComponent("item.hee.trinket_pouch.tooltip"))
		}
		
		ItemAbstractInfusable.onAddInformation(stack, lines)
	}
	
	@Sided(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean {
		return super.hasEffect(stack) || ItemAbstractInfusable.onHasEffect(stack)
	}
	
	@Sided(Side.CLIENT)
	@SubscribeEvent(EventPriority.LOWEST)
	fun onMouseInputPre(e: GuiScreenEvent.MouseClickedEvent.Pre) {
		val gui = e.gui
		
		if (gui is InventoryScreen && e.button == 1 && !Screen.hasShiftDown()) {
			val hoveredSlot = gui.slotUnderMouse
			
			if (hoveredSlot != null && hoveredSlot.stack.item === this) {
				val slotIndex = when (hoveredSlot) {
					is SlotTrinketItemInventory -> SlotTrinketItemInventory.INTERNAL_INDEX
					else                        -> hoveredSlot.slotIndex
				}
				
				PacketServerOpenInventoryItem(slotIndex).sendToServer()
				e.isCanceled = true
			}
		}
	}
}
