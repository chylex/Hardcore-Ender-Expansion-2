package chylex.hee.game.mechanics.trinket

import chylex.hee.game.container.slot.SlotTrinketItemInventory
import chylex.hee.game.mechanics.trinket.TrinketHandler.TrinketCapability.Provider
import chylex.hee.network.client.PacketClientTrinketBreak
import chylex.hee.system.facades.Resource
import chylex.hee.system.facades.Stats
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.forge.capability.CapabilityProvider
import chylex.hee.system.forge.capability.PlayerCapabilityHandler
import chylex.hee.system.forge.capability.PlayerCapabilityHandler.IPlayerCapability
import chylex.hee.system.forge.capability.getCapOrNull
import chylex.hee.system.forge.capability.register
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.readStack
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writeStack
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.GameRules.KEEP_INVENTORY
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.items.ItemStackHandler

object TrinketHandler {
	fun register() {
		CapabilityManager.INSTANCE.register<TrinketCapability>()
		PlayerCapabilityHandler.register(Handler)
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	fun get(player: EntityPlayer): ITrinketHandler {
		val currentItem = getTrinketSlotItem(player)
		val handlerProvider = currentItem.item as? ITrinketHandlerProvider
		
		return if (handlerProvider != null && handlerProvider.canPlaceIntoTrinketSlot(currentItem))
			handlerProvider.createTrinketHandler(player)
		else
			InternalHandlerFor(player)
	}
	
	fun getTrinketSlotItem(player: EntityPlayer): ItemStack {
		return player.getCapOrNull(CAP_TRINKET_SLOT)?.item ?: ItemStack.EMPTY
	}
	
	fun isInTrinketSlot(player: EntityPlayer, stack: ItemStack): Boolean {
		return getTrinketSlotItem(player) === stack || get(player).isInTrinketSlot(stack)
	}
	
	fun playTrinketBreakFX(player: EntityPlayer, item: Item) {
		player.addStat(Stats.useItem(item))
		PacketClientTrinketBreak(player, item).sendToAllAround(player, 32.0)
	}
	
	// Default handler implementation
	
	private class InternalHandlerFor(private val player: EntityPlayer) : ITrinketHandler {
		override fun isInTrinketSlot(stack: ItemStack): Boolean {
			return getTrinketSlotItem(player) === stack
		}
		
		override fun isItemActive(item: ITrinketItem): Boolean {
			return getTrinketIfActive(item) != null
		}
		
		override fun transformIfActive(item: ITrinketItem, transformer: (ItemStack) -> Unit) {
			val trinketItem = getTrinketIfActive(item)
			
			if (trinketItem != null) {
				transformer(trinketItem) // no need to refresh the stack
				
				if (!item.canPlaceIntoTrinketSlot(trinketItem)) {
					playTrinketBreakFX(player, trinketItem.item)
				}
			}
		}
		
		private fun getTrinketIfActive(item: ITrinketItem): ItemStack? {
			return getTrinketSlotItem(player).takeIf { it.item === item && item.canPlaceIntoTrinketSlot(it) }
		}
	}
	
	// Capability handling
	
	private object Handler : IPlayerCapability {
		override val key = Resource.Custom("trinket")
		override fun provide(player: EntityPlayer) = Provider()
	}
	
	@JvmStatic
	@CapabilityInject(TrinketCapability::class)
	private var CAP_TRINKET_SLOT: Capability<TrinketCapability>? = null
	
	private fun setTrinketSlotItem(player: EntityPlayer, stack: ItemStack) {
		player.getCapOrNull(CAP_TRINKET_SLOT)?.item = stack
	}
	
	@SubscribeEvent
	fun onEntityJoinWorld(e: EntityJoinWorldEvent) {
		val entity = e.entity
		
		if (entity is EntityPlayer) {
			val handler = entity.getCapOrNull(CAP_TRINKET_SLOT) ?: return
			
			with(entity.container) {
				if (inventorySlots.none { it is SlotTrinketItemInventory }) {
					inventorySlots.add(SlotTrinketItemInventory(handler, inventorySlots.size))
					inventoryItemStacks.add(handler.item)
				}
			}
		}
	}
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onPlayerDrops(e: LivingDropsEvent) {
		val player = e.entity as? EntityPlayer
		
		if (player == null) {
			return
		}
		
		val world = player.world
		val handler = player.getCapOrNull(CAP_TRINKET_SLOT)
		
		if (handler == null || handler.item.isEmpty || world.isRemote || world.gameRules.getBoolean(KEEP_INVENTORY)) {
			return
		}
		
		e.drops.add(player.dropItem(handler.item, true, false))
		handler.item = ItemStack.EMPTY
	}
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onPlayerClone(e: PlayerEvent.Clone) {
		val oldPlayer = e.original
		val newPlayer = e.player
		
		if (oldPlayer.world.gameRules.getBoolean(KEEP_INVENTORY)) {
			setTrinketSlotItem(newPlayer, getTrinketSlotItem(oldPlayer))
		}
	}
	
	private class TrinketCapability private constructor() : ItemStackHandler(1) {
		var item: ItemStack
			get()      = getStackInSlot(0)
			set(value) = setStackInSlot(0, value)
		
		override fun serializeNBT() = TagCompound().apply {
			writeStack(item)
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			item = readStack()
		}
		
		class Provider : CapabilityProvider<TrinketCapability, TagCompound>(CAP_TRINKET_SLOT, TrinketCapability())
	}
}
