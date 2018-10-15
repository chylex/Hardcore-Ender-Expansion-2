package chylex.hee.game.mechanics
import chylex.hee.game.gui.slot.SlotTrinketItem
import chylex.hee.game.mechanics.TrinketHandler.TrinketCapability.Provider
import chylex.hee.system.Resource
import chylex.hee.system.util.forge.capabilities.CapabilityProvider
import chylex.hee.system.util.forge.capabilities.NullFactory
import chylex.hee.system.util.forge.capabilities.NullStorage
import chylex.hee.system.util.readStack
import chylex.hee.system.util.writeStack
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.PlayerDropsEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.items.ItemStackHandler

object TrinketHandler{
	fun register(){
		CapabilityManager.INSTANCE.register(TrinketCapability::class.java, NullStorage.get(), NullFactory.get())
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	fun getCurrentItem(player: EntityPlayer): ItemStack{
		return player.getCapability(CAP_TRINKET_SLOT!!, null)?.item ?: ItemStack.EMPTY
	}
	
	fun setCurrentItem(player: EntityPlayer, stack: ItemStack){
		player.getCapability(CAP_TRINKET_SLOT!!, null)?.item = stack
	}
	
	// Internal handling
	
	private val CAP_KEY = Resource.Custom("trinket")
	
	@JvmStatic
	@CapabilityInject(TrinketCapability::class)
	private var CAP_TRINKET_SLOT: Capability<TrinketCapability>? = null
	
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
				inventorySlots.add(SlotTrinketItem(handler, inventorySlots.size))
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
		e.drops.add(player.dropItem(handler.item, true, false))
		player.captureDrops = false
		
		handler.item = ItemStack.EMPTY
	}
	
	private class TrinketCapability private constructor() : ItemStackHandler(1){
		var item: ItemStack
			get()      = getStackInSlot(0)
			set(value) = setStackInSlot(0, value)
		
		override fun serializeNBT(): NBTTagCompound{
			return NBTTagCompound().apply { writeStack(item) }
		}
		
		override fun deserializeNBT(nbt: NBTTagCompound){
			item = nbt.readStack()
		}
		
		class Provider : CapabilityProvider<TrinketCapability, NBTTagCompound>(CAP_TRINKET_SLOT!!, TrinketCapability())
	}
}
