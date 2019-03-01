package chylex.hee.game.mechanics
import chylex.hee.game.gui.slot.SlotTrinketItemInventory
import chylex.hee.game.item.trinket.ITrinketHandler
import chylex.hee.game.item.trinket.ITrinketHandlerProvider
import chylex.hee.game.item.trinket.ITrinketItem
import chylex.hee.game.mechanics.TrinketHandler.TrinketCapability.Provider
import chylex.hee.network.client.PacketClientTrinketBreak
import chylex.hee.system.Resource
import chylex.hee.system.util.forge.capabilities.CapabilityProvider
import chylex.hee.system.util.forge.capabilities.NullFactory
import chylex.hee.system.util.forge.capabilities.NullStorage
import chylex.hee.system.util.readStack
import chylex.hee.system.util.writeStack
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.PlayerDropsEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.items.ItemStackHandler

object TrinketHandler{
	fun register(){
		CapabilityManager.INSTANCE.register(TrinketCapability::class.java, NullStorage.get(), NullFactory.get())
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
		return player.getCapability(CAP_TRINKET_SLOT!!, null)?.item ?: ItemStack.EMPTY
	}
	
	fun isInTrinketSlot(player: EntityPlayer, stack: ItemStack): Boolean{
		return getTrinketSlotItem(player) === stack || get(player).isInTrinketSlot(stack)
	}
	
	fun playTrinketBreakFX(player: EntityPlayer, item: Item){
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
	
	private val CAP_KEY = Resource.Custom("trinket")
	
	@JvmStatic
	@CapabilityInject(TrinketCapability::class)
	private var CAP_TRINKET_SLOT: Capability<TrinketCapability>? = null
	
	private fun setTrinketSlotItem(player: EntityPlayer, stack: ItemStack){
		player.getCapability(CAP_TRINKET_SLOT!!, null)?.item = stack
	}
	
	@SubscribeEvent
	fun onAttachCapabilities(e: AttachCapabilitiesEvent<Entity>){
		if (e.`object` is EntityPlayer){
			e.addCapability(CAP_KEY, Provider())
		}
	}
	
	@SubscribeEvent
	fun onEntityJoinWorld(e: EntityJoinWorldEvent){
		val entity = e.entity
		
		if (entity is EntityPlayer){
			val handler = entity.getCapability(CAP_TRINKET_SLOT!!, null) ?: return
			
			with(entity.inventoryContainer){
				inventorySlots.add(SlotTrinketItemInventory(handler, inventorySlots.size))
				inventoryItemStacks.add(handler.item)
			}
		}
	}
	
	@SubscribeEvent(priority = HIGHEST)
	fun onPlayerDrops(e: PlayerDropsEvent){
		val player = e.entityPlayer
		val handler = player.getCapability(CAP_TRINKET_SLOT!!, null)
		
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
		
		class Provider : CapabilityProvider<TrinketCapability, NBTTagCompound>(CAP_TRINKET_SLOT!!, TrinketCapability())
	}
}
