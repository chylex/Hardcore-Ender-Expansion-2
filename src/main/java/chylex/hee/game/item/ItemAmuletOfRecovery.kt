package chylex.hee.game.item
import chylex.hee.game.container.ContainerAmuletOfRecovery
import chylex.hee.game.container.base.IInventoryFromPlayerItem
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.trinket.ITrinketItem
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.init.ModContainers
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
import chylex.hee.system.util.compatibility.MinecraftForgeEventBus
import chylex.hee.system.util.enchantmentMap
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.getListOfCompounds
import chylex.hee.system.util.getListOfItemStacks
import chylex.hee.system.util.getStack
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.nbtOrNull
import chylex.hee.system.util.over
import chylex.hee.system.util.putStack
import chylex.hee.system.util.setStack
import chylex.hee.system.util.writeTag
import io.netty.buffer.Unpooled
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack
import net.minecraft.item.Rarity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.NonNullList
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.GameRules.KEEP_INVENTORY
import net.minecraft.world.World
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.player.PlayerEvent

class ItemAmuletOfRecovery(properties: Properties) : ItemAbstractEnergyUser(properties), ITrinketItem{
	private companion object{
		private const val CONTENTS_TAG = "Contents"
		private const val RETRIEVAL_ENERGY_TAG = "RetrievalEnergy"
		
		private const val PLAYER_RESPAWN_ITEM_TAG = "AmuletOfRecovery"
		
		private const val SLOT_COUNT = 9 * 5
		
		private val SLOTS_MAIN    = ((9 * 0) + 0) until ((9 * 4) + 0)
		private val SLOTS_ARMOR   = ((9 * 4) + 0) until ((9 * 4) + 4)
		private val SLOTS_OFFHAND = ((9 * 4) + 4) until ((9 * 4) + 5)
		private val SLOTS_EXTRA   = ((9 * 4) + 5) until ((9 * 5) + 0)
		
		private val SLOTS_MAIN_HOTBAR = ((9 * 0) + 0) until ((9 * 1) + 0)
		private val SLOTS_MAIN_BULK   = ((9 * 1) + 0) until ((9 * 4) + 0)
		
		private val BACKFILL_SLOT_ORDER
			get() = sequenceOf(SLOTS_EXTRA, SLOTS_MAIN_BULK, SLOTS_MAIN_HOTBAR, SLOTS_OFFHAND).flatten() // no armor slots because contents aren't checked
		
		private fun isStackValid(stack: ItemStack): Boolean{
			return stack.item is ItemAmuletOfRecovery
		}
		
		private fun hasAnyContents(stack: ItemStack): Boolean{
			return stack.heeTagOrNull.hasKey(CONTENTS_TAG)
		}
		
		private fun processPlayerInventory(player: EntityPlayer, block: (inventory: NonNullList<ItemStack>, vanillaSlot: Int, savedSlot: Int) -> Unit){
			with(player.inventory){
				SLOTS_MAIN.forEachIndexed    { vanillaSlot, savedSlot -> block(mainInventory, vanillaSlot, savedSlot) }
				SLOTS_ARMOR.forEachIndexed   { vanillaSlot, savedSlot -> block(armorInventory, vanillaSlot, savedSlot) }
				SLOTS_OFFHAND.forEachIndexed { vanillaSlot, savedSlot -> block(offHandInventory, vanillaSlot, savedSlot) }
			}
		}
		
		private fun movePlayerInventoryToTrinket(player: EntityPlayer, trinketItem: ItemStack){
			val saved = Array<ItemStack>(SLOT_COUNT){ ItemStack.EMPTY }
			
			fun moveFromInventory(sourceInventory: NonNullList<ItemStack>, sourceSlot: Int, savedSlot: Int){
				saved[savedSlot] = sourceInventory[sourceSlot]
				sourceInventory[sourceSlot] = ItemStack.EMPTY
			}
			
			processPlayerInventory(player, ::moveFromInventory)
			trinketItem.heeTag.putList(CONTENTS_TAG, NBTItemStackList.of(saved.asIterable()))
		}
		
		private fun updateRetrievalCost(trinketItem: ItemStack){
			val buffer = Unpooled.buffer()
			
			var sumOfSlots = 0
			var sumOfSizes = 0
			var sumOfEnchantments = 0
			var sumOfFilteredTagSizes = 0
			
			with(trinketItem.heeTag){
				for(stack in getListOfItemStacks(CONTENTS_TAG)){
					if (stack.isNotEmpty){
						sumOfSlots += 1
						sumOfSizes += stack.count
						sumOfEnchantments += stack.enchantmentMap.values.sum()
						
						val nbtCopy = stack.nbtOrNull?.copy()
						
						if (nbtCopy != null){ // UPDATE 1.15
							// general
							nbtCopy.remove("Damage")
							nbtCopy.remove("Unbreakable")
							nbtCopy.remove("CanDestroy")
							nbtCopy.remove("CanPlaceOn")
							
							// enchantments
							nbtCopy.remove("Enchantments")
							nbtCopy.remove("StoredEnchantments")
							nbtCopy.remove("RepairCost")
							
							// attributes
							nbtCopy.remove("AttributeModifiers")
							nbtCopy.remove("HideFlags")
							
							// calculation & cleanup
							try{
								buffer.writeTag(nbtCopy)
								sumOfFilteredTagSizes += buffer.writerIndex().coerceAtMost(2500)
							}catch(e: Exception){
								// TODO handle
							}
							
							buffer.clear()
						}
					}
				}
				
				val totalCost = 10 + (sumOfSlots / 2) + (sumOfSizes / 50) + (sumOfEnchantments / 6) + (sumOfFilteredTagSizes / 100)
				putInt(RETRIEVAL_ENERGY_TAG, totalCost)
			}
		}
	}
	
	class ContainerProvider(private val stack: ItemStack, private val hand: Hand) : INamedContainerProvider{
		override fun getDisplayName(): ITextComponent{
			return stack.displayName
		}
		
		override fun createMenu(id: Int, inventory: PlayerInventory, player: EntityPlayer): Container{
			return ContainerAmuletOfRecovery(id, player, hand)
		}
	}
	
	class Inv(override val player: EntityPlayer, private val itemHeldIn: Hand) : Inventory(SLOT_COUNT), IInventoryFromPlayerItem{
		init{
			val heldItem = player.getHeldItem(itemHeldIn)
			
			if (isStackValid(heldItem)){
				heldItem.heeTag.getListOfItemStacks(CONTENTS_TAG).forEachIndexed(::setStack)
			}
		}
		
		fun moveToPlayerInventory(): Boolean{
			if (!isStackValid(player.getHeldItem(itemHeldIn))){
				return false
			}
			
			fun moveToInventory(targetInventory: NonNullList<ItemStack>, targetSlot: Int, sourceSlot: Int){
				if (targetInventory[targetSlot].isEmpty){
					targetInventory[targetSlot] = getStack(sourceSlot)
					setStack(sourceSlot, ItemStack.EMPTY)
				}
			}
			
			processPlayerInventory(player, ::moveToInventory)
			return true
		}
		
		override fun tryUpdatePlayerItem(): Boolean{
			val heldItem = player.getHeldItem(itemHeldIn)
			
			if (!isStackValid(heldItem)){
				return false
			}
			
			var isEmpty = true
			val newList = NBTItemStackList()
			
			for((_, stack) in allSlots){
				newList.append(stack)
				
				if (stack.isNotEmpty){
					isEmpty = false
				}
			}
			
			if (isEmpty){
				heldItem.heeTag.remove(CONTENTS_TAG)
			}
			else{
				heldItem.heeTag.putList(CONTENTS_TAG, newList)
			}
			
			return true
		}
	}
	
	// Initialization
	
	init{
		MinecraftForgeEventBus.register(this)
	}
	
	// Energy properties
	
	override fun getEnergyCapacity(stack: ItemStack) =
		Units(stack.heeTagOrNull?.getIntegerOrNull(RETRIEVAL_ENERGY_TAG) ?: 30)
	
	override fun getEnergyPerUse(stack: ItemStack) =
		1 over 1
	
	// Item handling
	
	override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean{
		return hasMaximumEnergy(stack) && !hasAnyContents(stack)
	}
	
	override fun chargeEnergyUnit(stack: ItemStack): Boolean{
		if (!super.chargeEnergyUnit(stack)){
			return false
		}
		
		if (hasMaximumEnergy(stack) && hasAnyContents(stack) && stack.heeTag.getListOfCompounds(CONTENTS_TAG).all { it.isEmpty }){
			stack.heeTag.remove(CONTENTS_TAG) // re-activate Trinket immediately after charging if the inventory is empty
		}
		
		return true
	}
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: Hand): ActionResult<ItemStack>{
		val stack = player.getHeldItem(hand)
		
		if (!hasAnyContents(stack) || !hasMaximumEnergy(stack)){
			return super.onItemRightClick(world, player, hand)
		}
		
		ModContainers.open(player, ContainerProvider(stack, hand), hand.ordinal)
		return ActionResult(SUCCESS, stack)
	}
	
	// Death events
	
	@SubscribeEvent(EventPriority.HIGH)
	fun onLivingDeath(e: LivingDeathEvent){
		val player = e.entityLiving as? EntityPlayer ?: return
		val world = player.world
		
		if (world.isRemote || world.gameRules.getBoolean(KEEP_INVENTORY)){
			return
		}
		
		TrinketHandler.get(player).transformIfActive(this){
			val trinketItem = it.copy()
			it.count = 0 // effectively removes the item from the trinket slot
			
			movePlayerInventoryToTrinket(player, trinketItem)
			setEnergyChargeLevel(trinketItem, Units(0))
			
			player.heeTag.putStack(PLAYER_RESPAWN_ITEM_TAG, trinketItem)
		}
	}
	
	@SubscribeEvent(EventPriority.LOW)
	fun onPlayerDrops(e: LivingDropsEvent){
		val player = e.entity
		val drops = e.drops
		
		if (player.world.isRemote || player !is EntityPlayer || drops.isEmpty()){
			return
		}
		
		with(player.heeTagOrNull?.getStack(PLAYER_RESPAWN_ITEM_TAG)?.heeTag ?: return){
			val list = getListOfItemStacks(CONTENTS_TAG)
			
			for(slot in BACKFILL_SLOT_ORDER){
				if (list.get(slot).isEmpty){
					list.set(slot, drops.first().item)
					drops.iterator().remove()
					
					if (drops.isEmpty()){
						break
					}
				}
			}
		}
	}
	
	@SubscribeEvent(EventPriority.LOWEST)
	fun onPlayerClone(e: PlayerEvent.Clone){
		if (!e.isWasDeath){
			return
		}
		
		val oldPlayer = e.original
		val newPlayer = e.player
		
		with(oldPlayer.heeTagOrNull ?: return){
			val trinketItem = getStack(PLAYER_RESPAWN_ITEM_TAG)
			
			if (trinketItem.isNotEmpty){
				updateRetrievalCost(trinketItem)
				
				if (!newPlayer.inventory.addItemStackToInventory(trinketItem)){
					newPlayer.dropItem(trinketItem, false, false)
				}
				
				remove(PLAYER_RESPAWN_ITEM_TAG)
			}
		}
	}
	
	// Client side
	
	override fun showDurabilityBar(stack: ItemStack): Boolean{
		return !hasMaximumEnergy(stack)
	}
	
	override fun getRarity(stack: ItemStack): Rarity{
		return ItemAbstractTrinket.onGetRarity()
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		if (!hasAnyContents(stack)){
			ItemAbstractTrinket.onAddInformation(stack, this, lines)
		}
		
		lines.add(TextComponentTranslation("item.tooltip.hee.energy.level", getEnergyChargeLevel(stack).units.value, getEnergyCapacity(stack).units.value))
	}
	
	@Sided(Side.CLIENT)
	override fun hasEffect(stack: ItemStack): Boolean{
		return hasAnyContents(stack)
	}
}
