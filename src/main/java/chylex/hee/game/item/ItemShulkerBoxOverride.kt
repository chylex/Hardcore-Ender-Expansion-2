package chylex.hee.game.item
import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockShulkerBoxOverride.BoxSize
import chylex.hee.game.block.entity.TileEntityShulkerBoxCustom
import chylex.hee.game.container.ContainerShulkerBoxInInventory
import chylex.hee.game.container.base.IInventoryFromPlayerItem
import chylex.hee.init.ModContainers
import chylex.hee.network.server.PacketServerOpenInventoryItem
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.MagicValues
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.ItemBlock
import chylex.hee.system.util.allSlots
import chylex.hee.system.util.facades.Stats
import chylex.hee.system.util.find
import chylex.hee.system.util.getCompoundOrNull
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.getOrCreateCompound
import chylex.hee.system.util.getStack
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.nbt
import chylex.hee.system.util.nbtOrNull
import chylex.hee.system.util.nonEmptySlots
import chylex.hee.system.util.putEnum
import chylex.hee.system.util.setStack
import chylex.hee.system.util.size
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.block.Block
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.inventory.InventoryScreen
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.NonNullList
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting.GRAY
import net.minecraft.util.text.TextFormatting.ITALIC
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraftforge.client.event.GuiScreenEvent

class ItemShulkerBoxOverride(block: Block, properties: Properties) : ItemBlock(block, properties){
	companion object{
		private const val TOOLTIP_ENTRY_COUNT = 5
		
		private fun isStackValid(stack: ItemStack): Boolean{
			return stack.item is ItemShulkerBoxOverride
		}
		
		fun getBoxSize(stack: ItemStack): BoxSize{
			return stack.nbtOrNull
				?.getCompoundOrNull(MagicValues.TILE_ENTITY_TAG)
				?.heeTagOrNull
				?.getEnum<BoxSize>(TileEntityShulkerBoxCustom.BOX_SIZE_TAG)
				?: BoxSize.LARGE
		}
		
		fun setBoxSize(stack: ItemStack, boxSize: BoxSize){
			stack.nbt.getOrCreateCompound(MagicValues.TILE_ENTITY_TAG).heeTag.putEnum(TileEntityShulkerBoxCustom.BOX_SIZE_TAG, boxSize)
		}
	}
	
	class ContainerProvider(private val stack: ItemStack, private val slot: Int) : INamedContainerProvider{
		override fun getDisplayName(): ITextComponent{
			return stack.displayName
		}
		
		override fun createMenu(id: Int, inventory: PlayerInventory, player: EntityPlayer): Container{
			return ContainerShulkerBoxInInventory(id, player, slot)
		}
	}
	
	class Inv(override val player: EntityPlayer, boxSize: BoxSize, private val inventorySlot: Int) : Inventory(boxSize.slots), IInventoryFromPlayerItem{
		private val boxStack
			get() = player.inventory.getStack(inventorySlot)
		
		init{
			val boxStack = boxStack
			
			if (isStackValid(boxStack)){
				NonNullList.withSize(size, ItemStack.EMPTY).also {
					ItemStackHelper.loadAllItems(boxStack.nbt.getCompound(MagicValues.TILE_ENTITY_TAG), it)
					it.forEachIndexed(::setStack)
				}
				
				player.addStat(Stats.OPEN_SHULKER_BOX)
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
				
				ItemStackHelper.saveAllItems(boxStack.nbt.getOrCreateCompound(MagicValues.TILE_ENTITY_TAG), it)
			}
			
			return true
		}
	}
	
	// Properties
	
	override fun getTranslationKey(stack: ItemStack): String{
		return getBoxSize(stack).translationKey
	}
	
	override fun fillItemGroup(group: ItemGroup, items: NonNullList<ItemStack>){
		if (isInGroup(group)){
			for(boxSize in BoxSize.values()){
				items.add(ItemStack(this).also { setBoxSize(it, boxSize) })
			}
		}
	}
	
	// Use handling
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: Hand): ActionResult<ItemStack>{
		val stack = player.getHeldItem(hand)
		val slot = player.inventory.nonEmptySlots.find { it.stack === stack }
		
		if (slot == null){
			return ActionResult(PASS, stack)
		}
		
		ModContainers.open(player, ContainerProvider(stack, slot.slot), slot.slot) // TODO it'd be pretty funny if the open animation was shown in inventory/held model but holy shit effort
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
					PacketServerOpenInventoryItem(hoveredSlot.slotIndex).sendToServer()
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
			lines.add(TranslationTextComponent("item.hee.shulker_box.tooltip"))
			lines.add(StringTextComponent(""))
		}
		
		val contentsTag = stack.nbtOrNull?.getCompoundOrNull(MagicValues.TILE_ENTITY_TAG)
		
		if (contentsTag != null){
			val inventory = NonNullList.withSize(BoxSize.LARGE.slots, ItemStack.EMPTY)
			ItemStackHelper.loadAllItems(contentsTag, inventory)
			
			if (inventory.any { it.isNotEmpty }){
				val counts = Object2IntOpenHashMap<String>()
				
				for(invStack in inventory){
					if (invStack.isNotEmpty){
						counts.addTo(invStack.displayName.string, invStack.count)
					}
				}
				
				val sorted = counts.object2IntEntrySet().sortedWith(compareBy({ -it.intValue }, { it.key }))
				
				for((name, count) in sorted.take(TOOLTIP_ENTRY_COUNT)){
					lines.add(StringTextComponent("%s x%d".format(name, count)).applyTextStyle(GRAY))
				}
				
				if (sorted.size > TOOLTIP_ENTRY_COUNT){
					lines.add(TranslationTextComponent("container.shulkerBox.more", sorted.size - TOOLTIP_ENTRY_COUNT).applyTextStyles(GRAY, ITALIC))
				}
			}
		}
		
		if ((lines.lastOrNull() as? StringTextComponent)?.let { it.text.isEmpty() && it.siblings.isEmpty() } == true){
			lines.removeAt(lines.lastIndex)
		}
	}
}
