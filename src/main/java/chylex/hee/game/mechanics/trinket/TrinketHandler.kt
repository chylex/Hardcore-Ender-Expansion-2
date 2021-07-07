package chylex.hee.game.mechanics.trinket

import chylex.hee.game.Resource
import chylex.hee.game.container.slot.SlotTrinketItemInventory
import chylex.hee.game.entity.player.PlayerCapabilityHandler
import chylex.hee.game.entity.player.PlayerCapabilityHandler.IPlayerCapability
import chylex.hee.game.mechanics.trinket.TrinketHandler.TrinketCapability.Provider
import chylex.hee.network.client.PacketClientTrinketBreak
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.forge.capability.CapabilityProvider
import chylex.hee.util.forge.capability.getCapOrNull
import chylex.hee.util.forge.capability.register
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.readStack
import chylex.hee.util.nbt.use
import chylex.hee.util.nbt.writeStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.stats.Stats
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
	
	fun get(player: PlayerEntity): ITrinketHandler {
		val currentItem = getTrinketSlotItem(player)
		val handlerProvider = currentItem.item as? ITrinketHandlerProvider
		
		return if (handlerProvider != null && handlerProvider.canPlaceIntoTrinketSlot(currentItem))
			handlerProvider.createTrinketHandler(player)
		else
			InternalHandlerFor(player)
	}
	
	fun getTrinketSlotItem(player: PlayerEntity): ItemStack {
		return player.getCapOrNull(CAP_TRINKET_SLOT)?.item ?: ItemStack.EMPTY
	}
	
	fun isInTrinketSlot(player: PlayerEntity, stack: ItemStack): Boolean {
		return getTrinketSlotItem(player) === stack || get(player).isInTrinketSlot(stack)
	}
	
	fun playTrinketBreakFX(player: PlayerEntity, item: Item) {
		player.addStat(Stats.ITEM_USED[item])
		PacketClientTrinketBreak(player, item).sendToAllAround(player, 32.0)
	}
	
	// Default handler implementation
	
	private class InternalHandlerFor(private val player: PlayerEntity) : ITrinketHandler {
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
		override fun provide(player: PlayerEntity) = Provider()
	}
	
	@JvmStatic
	@CapabilityInject(TrinketCapability::class)
	private var CAP_TRINKET_SLOT: Capability<TrinketCapability>? = null
	
	private fun setTrinketSlotItem(player: PlayerEntity, stack: ItemStack) {
		player.getCapOrNull(CAP_TRINKET_SLOT)?.item = stack
	}
	
	@SubscribeEvent
	fun onEntityJoinWorld(e: EntityJoinWorldEvent) {
		val entity = e.entity
		
		if (entity is PlayerEntity) {
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
		val player = e.entity as? PlayerEntity
		
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
