package chylex.hee.game.item

import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.game.MagicValues
import chylex.hee.game.block.BlockShulkerBoxOverride.BoxSize
import chylex.hee.game.block.entity.TileEntityShulkerBoxCustom
import chylex.hee.game.container.ContainerShulkerBoxInInventory
import chylex.hee.game.inventory.util.IInventoryFromPlayerItem
import chylex.hee.game.inventory.util.allSlots
import chylex.hee.game.inventory.util.getStack
import chylex.hee.game.inventory.util.nonEmptySlots
import chylex.hee.game.inventory.util.setStack
import chylex.hee.game.inventory.util.size
import chylex.hee.game.item.builder.HeeBlockItemBuilder
import chylex.hee.game.item.components.ICreativeTabComponent
import chylex.hee.game.item.components.IItemNameComponent
import chylex.hee.game.item.components.IReequipAnimationComponent
import chylex.hee.game.item.components.ITooltipComponent
import chylex.hee.game.item.components.IUseItemOnAirComponent
import chylex.hee.game.item.container.IItemWithContainer
import chylex.hee.game.item.interfaces.getHeeInterface
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.util.isNotEmpty
import chylex.hee.game.item.util.nbt
import chylex.hee.game.item.util.nbtOrNull
import chylex.hee.init.ModContainers
import chylex.hee.network.server.PacketServerOpenInventoryItem
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.collection.find
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.nbt.getCompoundOrNull
import chylex.hee.util.nbt.getEnum
import chylex.hee.util.nbt.getOrCreateCompound
import chylex.hee.util.nbt.putEnum
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.block.Block
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.inventory.InventoryScreen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.stats.Stats
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.NonNullList
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting.GRAY
import net.minecraft.util.text.TextFormatting.ITALIC
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraftforge.client.event.GuiScreenEvent

class ItemShulkerBoxOverride(block: Block) : HeeBlockItemBuilder(block) {
	companion object {
		private const val TOOLTIP_ENTRY_COUNT = 5
		
		private const val LANG_TOOLTIP_OPEN = "item.hee.shulker_box.tooltip"
		
		fun getBoxSize(stack: ItemStack): BoxSize {
			return stack.nbtOrNull
				?.getCompoundOrNull(MagicValues.TILE_ENTITY_TAG)
				?.heeTagOrNull
				?.getEnum<BoxSize>(TileEntityShulkerBoxCustom.BOX_SIZE_TAG)
				?: BoxSize.LARGE
		}
		
		fun setBoxSize(stack: ItemStack, boxSize: BoxSize) {
			stack.nbt.getOrCreateCompound(MagicValues.TILE_ENTITY_TAG).heeTag.putEnum(TileEntityShulkerBoxCustom.BOX_SIZE_TAG, boxSize)
		}
		
		private val CONTAINER = IItemWithContainer(::ContainerProvider)
		
		fun isItemValid(item: Item): Boolean {
			return item.getHeeInterface<IItemWithContainer>() === CONTAINER
		}
	}
	
	init {
		localizationExtra[LANG_TOOLTIP_OPEN] = "§7Right-click to open"
		
		model = ItemModel.Manual
		
		maxStackSize = 1
		
		components.name = object : IItemNameComponent {
			override fun getTranslationKey(stack: ItemStack): String {
				return getBoxSize(stack).translationKey
			}
		}
		
		components.tooltip.add(ITooltipComponent { lines, stack, _, _ ->
			if (MC.currentScreen is InventoryScreen) {
				lines.add(TranslationTextComponent(LANG_TOOLTIP_OPEN))
				lines.add(StringTextComponent(""))
			}
			
			val contentsTag = stack.nbtOrNull?.getCompoundOrNull(MagicValues.TILE_ENTITY_TAG)
			
			if (contentsTag != null) {
				val inventory = NonNullList.withSize(BoxSize.LARGE.slots, ItemStack.EMPTY)
				ItemStackHelper.loadAllItems(contentsTag, inventory)
				
				if (inventory.any(ItemStack::isNotEmpty)) {
					val counts = Object2IntOpenHashMap<String>()
					
					for (invStack in inventory) {
						if (invStack.isNotEmpty) {
							counts.addTo(invStack.displayName.string, invStack.count)
						}
					}
					
					val sorted = counts.object2IntEntrySet().sortedWith(compareBy({ -it.intValue }, { it.key }))
					
					for ((name, count) in sorted.take(TOOLTIP_ENTRY_COUNT)) {
						lines.add(StringTextComponent("%s x%d".format(name, count)).mergeStyle(GRAY))
					}
					
					if (sorted.size > TOOLTIP_ENTRY_COUNT) {
						lines.add(TranslationTextComponent("container.shulkerBox.more", sorted.size - TOOLTIP_ENTRY_COUNT).mergeStyle(GRAY, ITALIC))
					}
				}
			}
			
			if ((lines.lastOrNull() as? StringTextComponent)?.let { it.text.isEmpty() && it.siblings.isEmpty() } == true) {
				lines.removeAt(lines.lastIndex)
			}
		})
		
		components.reequipAnimation = IReequipAnimationComponent.AnimateIfSlotChanged
		components.creativeTab = ICreativeTabComponent { tab, item ->
			for (boxSize in BoxSize.values()) {
				tab.add(ItemStack(item).also { setBoxSize(it, boxSize) })
			}
		}
		
		components.useOnAir = object : IUseItemOnAirComponent {
			override fun use(world: World, player: PlayerEntity, hand: Hand, heldItem: ItemStack): ActionResult<ItemStack> {
				val slot = player.inventory.nonEmptySlots.find { it.stack === heldItem }
				if (slot == null) {
					return ActionResult(PASS, heldItem)
				}
				
				ModContainers.open(player, ContainerProvider(heldItem, slot.slot), slot.slot) // POLISH it'd be pretty funny if the open animation was shown in inventory/held model but holy shit effort
				return ActionResult(SUCCESS, heldItem)
			}
		}
		
		interfaces[IItemWithContainer::class.java] = CONTAINER
	}
	
	@Sided(Side.CLIENT)
	@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
	object EventHandler {
		@SubscribeEvent(EventPriority.LOWEST)
		fun onMouseInputPre(e: GuiScreenEvent.MouseClickedEvent.Pre) {
			val gui = e.gui
			
			if (gui is InventoryScreen && e.button == 1 && !Screen.hasShiftDown()) {
				val hoveredSlot = gui.slotUnderMouse
				
				if (hoveredSlot != null && isItemValid(hoveredSlot.stack.item)) {
					PacketServerOpenInventoryItem(hoveredSlot.slotIndex).sendToServer()
					e.isCanceled = true
				}
			}
		}
	}
	
	class ContainerProvider(private val stack: ItemStack, private val slot: Int) : INamedContainerProvider {
		override fun getDisplayName(): ITextComponent {
			return stack.displayName
		}
		
		override fun createMenu(id: Int, inventory: PlayerInventory, player: PlayerEntity): Container {
			return ContainerShulkerBoxInInventory(id, player, slot)
		}
	}
	
	class Inv(override val player: PlayerEntity, boxSize: BoxSize, private val inventorySlot: Int) : Inventory(boxSize.slots), IInventoryFromPlayerItem {
		private val boxStack
			get() = player.inventory.getStack(inventorySlot)
		
		init {
			val boxStack = boxStack
			
			if (isItemValid(boxStack.item)) {
				NonNullList.withSize(size, ItemStack.EMPTY).also {
					ItemStackHelper.loadAllItems(boxStack.nbt.getCompound(MagicValues.TILE_ENTITY_TAG), it)
					it.forEachIndexed(::setStack)
				}
				
				player.addStat(Stats.OPEN_SHULKER_BOX)
			}
		}
		
		override fun tryUpdatePlayerItem(): Boolean {
			val boxStack = boxStack
			
			if (!isItemValid(boxStack.item)) {
				return false
			}
			
			NonNullList.withSize(size, ItemStack.EMPTY).also {
				for ((slot, stack) in allSlots) {
					it[slot] = stack
				}
				
				ItemStackHelper.saveAllItems(boxStack.nbt.getOrCreateCompound(MagicValues.TILE_ENTITY_TAG), it)
			}
			
			return true
		}
	}
}
