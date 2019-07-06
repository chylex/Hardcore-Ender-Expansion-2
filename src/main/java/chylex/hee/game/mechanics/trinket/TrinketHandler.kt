package chylex.hee.game.mechanics.trinket
import chylex.hee.game.container.slot.SlotTrinketItemInventory
import chylex.hee.game.mechanics.trinket.TrinketHandler.TrinketCapability.Provider
import chylex.hee.network.client.PacketClientTrinketBreak
import chylex.hee.system.Resource
import chylex.hee.system.capability.CapabilityProvider
import chylex.hee.system.capability.PlayerCapabilityHandler
import chylex.hee.system.capability.PlayerCapabilityHandler.IPlayerCapability
import chylex.hee.system.util.getCapOrNull
import chylex.hee.system.util.readStack
import chylex.hee.system.util.register
import chylex.hee.system.util.writeStack
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.stats.StatList
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.PlayerDropsEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.items.ItemStackHandler

object TrinketHandler{
	fun register(){
		CapabilityManager.INSTANCE.register<TrinketCapability>()
		PlayerCapabilityHandler.register(Handler)
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	fun get(player: EntityPlayer) : ITrinketHandler{
		val currentItem = getTrinketSlotItem(player)
		val handlerProvider = currentItem.item as? ITrinketHandlerProvider
		
		return if (handlerProvider != null && handlerProvider.canPlaceIntoTrinketSlot(currentItem))
			handlerProvider.createTrinketHandler(player)
		else
			InternalHandlerFor(player)
	}
	
	fun getTrinketSlotItem(player: EntityPlayer): ItemStack{
		return player.getCapOrNull(CAP_TRINKET_SLOT)?.item ?: ItemStack.EMPTY
	}
	
	fun isInTrinketSlot(player: EntityPlayer, stack: ItemStack): Boolean{
		return getTrinketSlotItem(player) === stack || get(player).isInTrinketSlot(stack)
	}
	
	fun playTrinketBreakFX(player: EntityPlayer, item: Item){
		player.addStat(StatList.getObjectUseStats(item)!!)
		PacketClientTrinketBreak(player, item).sendToAllAround(player, 32.0)
	}
	
	// Default handler implementation
	
	private class InternalHandlerFor(private val player: EntityPlayer) : ITrinketHandler{
		override fun isInTrinketSlot(stack: ItemStack): Boolean{
			return getTrinketSlotItem(player) === stack
		}
		
		override fun isItemActive(item: ITrinketItem): Boolean{
			return getTrinketIfActive(item) != null
		}
		
		override fun transformIfActive(item: ITrinketItem, transformer: (ItemStack) -> Unit){
			val trinketItem = getTrinketIfActive(item)
			
			if (trinketItem != null){
				transformer(trinketItem) // no need to refresh the stack
				
				if (!item.canPlaceIntoTrinketSlot(trinketItem)){
					playTrinketBreakFX(player, trinketItem.item)
				}
			}
		}
		
		private fun getTrinketIfActive(item: ITrinketItem): ItemStack?{
			return getTrinketSlotItem(player).takeIf { it.item === item && item.canPlaceIntoTrinketSlot(it) }
		}
	}
	
	// Capability handling
	
	private object Handler : IPlayerCapability{
		override val key = Resource.Custom("trinket")
		override fun provide(player: EntityPlayer) = Provider()
	}
	
	@JvmStatic
	@CapabilityInject(TrinketCapability::class)
	private var CAP_TRINKET_SLOT: Capability<TrinketCapability>? = null
	
	private fun setTrinketSlotItem(player: EntityPlayer, stack: ItemStack){
		player.getCapOrNull(CAP_TRINKET_SLOT)?.item = stack
	}
	
	@SubscribeEvent
	fun onEntityJoinWorld(e: EntityJoinWorldEvent){
		val entity = e.entity
		
		if (entity is EntityPlayer){
			val handler = entity.getCapOrNull(CAP_TRINKET_SLOT) ?: return
			
			with(entity.inventoryContainer){
				if (inventorySlots.none { it is SlotTrinketItemInventory }){
					inventorySlots.add(SlotTrinketItemInventory(handler, inventorySlots.size))
					inventoryItemStacks.add(handler.item)
				}
			}
		}
	}
	
	@SubscribeEvent(priority = HIGHEST)
	fun onPlayerDrops(e: PlayerDropsEvent){
		val player = e.entityPlayer
		val handler = player.getCapOrNull(CAP_TRINKET_SLOT)
		
		if (handler == null || handler.item.isEmpty){
			return
		}
		
		player.captureDrops = true
		player.dropItem(handler.item, true, false)
		player.captureDrops = false
		
		handler.item = ItemStack.EMPTY
	}
	
	@SubscribeEvent(priority = HIGHEST)
	fun onPlayerClone(e: PlayerEvent.Clone){
		val oldPlayer = e.original
		val newPlayer = e.entityPlayer
		
		if (oldPlayer.world.gameRules.getBoolean("keepInventory")){
			setTrinketSlotItem(newPlayer, getTrinketSlotItem(oldPlayer))
		}
	}
	
	private class TrinketCapability private constructor() : ItemStackHandler(1){
		var item: ItemStack
			get()      = getStackInSlot(0)
			set(value) = setStackInSlot(0, value)
		
		override fun serializeNBT() = NBTTagCompound().apply {
			writeStack(item)
		}
		
		override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
			item = readStack()
		}
		
		class Provider : CapabilityProvider<TrinketCapability, NBTTagCompound>(CAP_TRINKET_SLOT, TrinketCapability())
	}
}
