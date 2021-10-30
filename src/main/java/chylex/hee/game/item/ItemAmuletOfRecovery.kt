package chylex.hee.game.item

import chylex.hee.HEE
import chylex.hee.game.container.ContainerAmuletOfRecovery
import chylex.hee.game.inventory.util.IInventoryFromPlayerItem
import chylex.hee.game.inventory.util.allSlots
import chylex.hee.game.inventory.util.getStack
import chylex.hee.game.inventory.util.setStack
import chylex.hee.game.item.ItemAbstractEnergyUser.EnergyDurabilityComponent
import chylex.hee.game.item.ItemAbstractEnergyUser.EnergyItem
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.IItemGlintComponent
import chylex.hee.game.item.components.ITooltipComponent
import chylex.hee.game.item.components.IUseItemOnAirComponent
import chylex.hee.game.item.interfaces.getHeeInterface
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.util.enchantmentMap
import chylex.hee.game.item.util.isNotEmpty
import chylex.hee.game.item.util.nbtOrNull
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.init.ModContainers
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.buffer.writeTag
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.math.over
import chylex.hee.util.nbt.NBTItemStackList
import chylex.hee.util.nbt.getIntegerOrNull
import chylex.hee.util.nbt.getListOfCompounds
import chylex.hee.util.nbt.getListOfItemStacks
import chylex.hee.util.nbt.getStack
import chylex.hee.util.nbt.hasKey
import chylex.hee.util.nbt.putList
import chylex.hee.util.nbt.putStack
import io.netty.buffer.Unpooled
import net.minecraft.command.ICommandSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.NonNullList
import net.minecraft.util.Util
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting.DARK_RED
import net.minecraft.util.text.TextFormatting.RED
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.GameRules.KEEP_INVENTORY
import net.minecraft.world.World
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import org.apache.commons.lang3.math.Fraction

@SubscribeAllEvents(modid = HEE.ID)
object ItemAmuletOfRecovery : HeeItemBuilder() {
	private const val CONTENTS_TAG = "Contents"
	private const val RETRIEVAL_ENERGY_TAG = "RetrievalEnergy"
	
	private const val PLAYER_RESPAWN_ITEM_TAG = "AmuletOfRecovery"
	
	private const val LANG_COST_ERROR = "item.hee.amulet_of_recovery.cost_error"
	
	private const val SLOT_COUNT = 9 * 5
	
	private val SLOTS_MAIN    = ((9 * 0) + 0) until ((9 * 4) + 0)
	private val SLOTS_ARMOR   = ((9 * 4) + 0) until ((9 * 4) + 4)
	private val SLOTS_OFFHAND = ((9 * 4) + 4) until ((9 * 4) + 5)
	private val SLOTS_EXTRA   = ((9 * 4) + 5) until ((9 * 5) + 0)
	
	private val SLOTS_MAIN_HOTBAR = ((9 * 0) + 0) until ((9 * 1) + 0)
	private val SLOTS_MAIN_BULK   = ((9 * 1) + 0) until ((9 * 4) + 0)
	
	private val BACKFILL_SLOT_ORDER
		get() = sequenceOf(SLOTS_EXTRA, SLOTS_MAIN_BULK, SLOTS_MAIN_HOTBAR, SLOTS_OFFHAND).flatten() // no armor slots because contents aren't checked
	
	private val ENERGY = object : EnergyItem() {
		override fun getEnergyCapacity(stack: ItemStack): Units {
			return Units(stack.heeTagOrNull?.getIntegerOrNull(RETRIEVAL_ENERGY_TAG) ?: 30)
		}
		
		override fun getEnergyPerUse(stack: ItemStack): Fraction {
			return 1 over 1
		}
		
		override fun onUnitCharged(stack: ItemStack) {
			if (hasMaximumEnergy(stack) && hasAnyContents(stack) && stack.heeTag.getListOfCompounds(CONTENTS_TAG).all { it.isEmpty }) {
				stack.heeTag.remove(CONTENTS_TAG) // re-activate Trinket immediately after charging if the inventory is empty
			}
		}
	}
	
	private val TRINKET = object : ITrinketItem {
		override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean {
			return ENERGY.hasMaximumEnergy(stack) && !hasAnyContents(stack)
		}
	}
	
	init {
		includeFrom(ItemAbstractTrinket(TRINKET))
		
		components.tooltip.replaceAll { original ->
			ITooltipComponent { lines, stack, advanced, world ->
				if (!hasAnyContents(stack)) {
					original.add(lines, stack, advanced, world)
				}
			}
		}
		
		includeFrom(ItemAbstractEnergyUser(ENERGY))
		
		localizationExtra[LANG_COST_ERROR] = "Error calculating Energy cost for Amulet of Recovery. Please check server logs for the full error and report it."
		
		model = ItemModel.Multi(
			ItemModel.Simple,
			ItemModel.Suffixed("_held")
		)
		
		maxStackSize = 1
		
		components.tooltip.add(ITooltipComponent { lines, stack, _, _ ->
			lines.add(TranslationTextComponent("item.tooltip.hee.energy.level", ENERGY.getChargeLevel(stack).units.value, ENERGY.getEnergyCapacity(stack).units.value))
		})
		
		components.useOnAir = object : IUseItemOnAirComponent {
			override fun use(world: World, player: PlayerEntity, hand: Hand, heldItem: ItemStack): ActionResult<ItemStack> {
				if (!hasAnyContents(heldItem) || !ENERGY.hasMaximumEnergy(heldItem)) {
					return ActionResult.resultPass(heldItem)
				}
				
				ModContainers.open(player, ContainerProvider(heldItem, hand), hand.ordinal)
				return ActionResult.resultSuccess(heldItem)
			}
		}
		
		components.durability = object : EnergyDurabilityComponent(ENERGY) {
			override fun showBar(stack: ItemStack): Boolean {
				return !ENERGY.hasMaximumEnergy(stack)
			}
		}
		
		components.glint = IItemGlintComponent(::hasAnyContents)
	}
	
	private fun isStackValid(stack: ItemStack): Boolean {
		return stack.item.getHeeInterface<ITrinketItem>() === TRINKET
	}
	
	private fun hasAnyContents(stack: ItemStack): Boolean {
		return stack.heeTagOrNull.hasKey(CONTENTS_TAG)
	}
	
	private fun processPlayerInventory(player: PlayerEntity, block: (inventory: NonNullList<ItemStack>, vanillaSlot: Int, savedSlot: Int) -> Unit) {
		with(player.inventory) {
			SLOTS_MAIN.forEachIndexed { vanillaSlot, savedSlot -> block(mainInventory, vanillaSlot, savedSlot) }
			SLOTS_ARMOR.forEachIndexed { vanillaSlot, savedSlot -> block(armorInventory, vanillaSlot, savedSlot) }
			SLOTS_OFFHAND.forEachIndexed { vanillaSlot, savedSlot -> block(offHandInventory, vanillaSlot, savedSlot) }
		}
	}
	
	private fun movePlayerInventoryToTrinket(player: PlayerEntity, trinketItem: ItemStack) {
		val saved = Array<ItemStack>(SLOT_COUNT) { ItemStack.EMPTY }
		
		fun moveFromInventory(sourceInventory: NonNullList<ItemStack>, sourceSlot: Int, savedSlot: Int) {
			saved[savedSlot] = sourceInventory[sourceSlot]
			sourceInventory[sourceSlot] = ItemStack.EMPTY
		}
		
		processPlayerInventory(player, ::moveFromInventory)
		trinketItem.heeTag.putList(CONTENTS_TAG, NBTItemStackList.of(saved.asIterable()))
	}
	
	private fun updateRetrievalCost(errorMessenger: ICommandSource, trinketItem: ItemStack) {
		val buffer = Unpooled.buffer()
		
		var sumOfSlots = 0
		var sumOfSizes = 0
		var sumOfEnchantments = 0
		var sumOfFilteredTagSizes = 0
		
		with(trinketItem.heeTag) {
			for (stack in getListOfItemStacks(CONTENTS_TAG)) {
				if (stack.isNotEmpty) {
					sumOfSlots += 1
					sumOfSizes += stack.count
					sumOfEnchantments += stack.enchantmentMap.values.sum()
					
					val nbtCopy = stack.nbtOrNull?.copy()
					
					if (nbtCopy != null) { // UPDATE 1.16
						// general
						nbtCopy.remove("Damage")
						nbtCopy.remove("Unbreakable")
						nbtCopy.remove("CanDestroy")
						nbtCopy.remove("CanPlaceOn")
						nbtCopy.remove("CustomModelData")
						
						// enchantments
						nbtCopy.remove("Enchantments")
						nbtCopy.remove("StoredEnchantments")
						nbtCopy.remove("RepairCost")
						
						// attributes
						nbtCopy.remove("AttributeModifiers")
						nbtCopy.remove("HideFlags")
						
						// calculation & cleanup
						try {
							buffer.writeTag(nbtCopy)
							sumOfFilteredTagSizes += buffer.writerIndex().coerceAtMost(2500)
						} catch (e: Exception) {
							sumOfFilteredTagSizes += 2500
							HEE.log.error("[ItemAmuletOfRecovery] failed processing NBT data when calculating Energy cost", e)
							
							errorMessenger.sendMessage(TranslationTextComponent(LANG_COST_ERROR).mergeStyle(RED), Util.DUMMY_UUID)
							e.message?.let { errorMessenger.sendMessage(StringTextComponent(it).mergeStyle(DARK_RED), Util.DUMMY_UUID) }
						}
						
						buffer.clear()
					}
				}
			}
			
			val totalCost = 10 + (sumOfSlots / 2) + (sumOfSizes / 50) + (sumOfEnchantments / 6) + (sumOfFilteredTagSizes / 100)
			putInt(RETRIEVAL_ENERGY_TAG, totalCost)
		}
	}
	
	class ContainerProvider(private val stack: ItemStack, private val hand: Hand) : INamedContainerProvider {
		override fun getDisplayName(): ITextComponent {
			return stack.displayName
		}
		
		override fun createMenu(id: Int, inventory: PlayerInventory, player: PlayerEntity): Container {
			return ContainerAmuletOfRecovery(id, player, hand)
		}
	}
	
	class Inv(override val player: PlayerEntity, private val itemHeldIn: Hand) : Inventory(SLOT_COUNT), IInventoryFromPlayerItem {
		init {
			val heldItem = player.getHeldItem(itemHeldIn)
			
			if (isStackValid(heldItem)) {
				heldItem.heeTag.getListOfItemStacks(CONTENTS_TAG).forEachIndexed(::setStack)
			}
		}
		
		fun moveToPlayerInventory(): Boolean {
			if (!isStackValid(player.getHeldItem(itemHeldIn))) {
				return false
			}
			
			fun moveToInventory(targetInventory: NonNullList<ItemStack>, targetSlot: Int, sourceSlot: Int) {
				if (targetInventory[targetSlot].isEmpty) {
					targetInventory[targetSlot] = getStack(sourceSlot)
					setStack(sourceSlot, ItemStack.EMPTY)
				}
			}
			
			processPlayerInventory(player, ::moveToInventory)
			return true
		}
		
		override fun tryUpdatePlayerItem(): Boolean {
			val heldItem = player.getHeldItem(itemHeldIn)
			
			if (!isStackValid(heldItem)) {
				return false
			}
			
			var isEmpty = true
			val newList = NBTItemStackList()
			
			for ((_, stack) in allSlots) {
				newList.append(stack)
				
				if (stack.isNotEmpty) {
					isEmpty = false
				}
			}
			
			if (isEmpty) {
				heldItem.heeTag.remove(CONTENTS_TAG)
			}
			else {
				heldItem.heeTag.putList(CONTENTS_TAG, newList)
			}
			
			return true
		}
	}
	
	// Death events
	
	@SubscribeEvent(EventPriority.HIGH)
	fun onLivingDeath(e: LivingDeathEvent) {
		val player = e.entityLiving as? PlayerEntity ?: return
		val world = player.world
		
		if (world.isRemote || world.gameRules.getBoolean(KEEP_INVENTORY)) {
			return
		}
		
		TrinketHandler.get(player).transformIfActive(TRINKET) {
			val trinketItem = it.copy()
			it.count = 0 // effectively removes the item from the trinket slot
			
			movePlayerInventoryToTrinket(player, trinketItem)
			ENERGY.setChargeLevel(trinketItem, Units(0))
			
			player.heeTag.putStack(PLAYER_RESPAWN_ITEM_TAG, trinketItem)
		}
	}
	
	@SubscribeEvent(EventPriority.LOW)
	fun onPlayerDrops(e: LivingDropsEvent) {
		val player = e.entity
		val drops = e.drops
		
		if (player.world.isRemote || player !is PlayerEntity || drops.isEmpty()) {
			return
		}
		
		with(player.heeTagOrNull?.getStack(PLAYER_RESPAWN_ITEM_TAG)?.heeTag ?: return) {
			val list = getListOfItemStacks(CONTENTS_TAG).takeIf { it.size >= SLOT_COUNT } ?: return
			val iter = drops.iterator()
			
			for (slot in BACKFILL_SLOT_ORDER) {
				if (list.get(slot).isEmpty) {
					list.set(slot, iter.next().item)
					iter.remove()
					
					if (!iter.hasNext()) {
						break
					}
				}
			}
		}
	}
	
	@SubscribeEvent(EventPriority.LOWEST)
	fun onPlayerClone(e: PlayerEvent.Clone) {
		if (!e.isWasDeath) {
			return
		}
		
		val oldPlayer = e.original
		val newPlayer = e.player
		
		with(oldPlayer.heeTagOrNull ?: return) {
			val trinketItem = getStack(PLAYER_RESPAWN_ITEM_TAG)
			
			if (trinketItem.isNotEmpty) {
				updateRetrievalCost(newPlayer, trinketItem)
				
				if (!newPlayer.inventory.addItemStackToInventory(trinketItem)) {
					newPlayer.dropItem(trinketItem, false, false)
				}
				
				remove(PLAYER_RESPAWN_ITEM_TAG)
			}
		}
	}
}
