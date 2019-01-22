package chylex.hee.game.mechanics.causatum
import chylex.hee.game.mechanics.causatum.EnderCausatum.CausatumCapability.Provider
import chylex.hee.system.Resource
import chylex.hee.system.util.forge.capabilities.CapabilityProvider
import chylex.hee.system.util.forge.capabilities.NullFactory
import chylex.hee.system.util.forge.capabilities.NullStorage
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EnderCausatum{
	fun register(){
		CapabilityManager.INSTANCE.register(CausatumCapability::class.java, NullStorage.get(), NullFactory.get())
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	fun getStage(player: EntityPlayer): CausatumStage{
		return getInstance(player).stage
	}
	
	fun triggerStage(player: EntityPlayer, newStage: CausatumStage, force: Boolean = false): Boolean{
		with(getInstance(player)){
			if (newStage > stage || force){
				stage = newStage
				return true
			}
		}
		
		return false
	}
	
	// Capability handling
	
	private val CAP_KEY = Resource.Custom("causatum")
	
	@JvmStatic
	@CapabilityInject(CausatumCapability::class)
	private var CAP_CAUSATUM: Capability<CausatumCapability>? = null
	
	private fun getInstance(player: EntityPlayer): CausatumCapability{
		return player.getCapability(CAP_CAUSATUM!!, null)!!
	}
	
	@SubscribeEvent
	fun onAttachCapabilities(e: AttachCapabilitiesEvent<Entity>){
		if (e.`object` is EntityPlayer){
			e.addCapability(CAP_KEY, Provider())
		}
	}
	
	@SubscribeEvent(priority = HIGHEST)
	fun onPlayerClone(e: PlayerEvent.Clone){
		val oldPlayer = e.original
		val newPlayer = e.entityPlayer
		
		getInstance(newPlayer).deserializeNBT(getInstance(oldPlayer).serializeNBT())
	}
	
	private class CausatumCapability private constructor() : INBTSerializable<NBTTagCompound>{
		var stage = CausatumStage.S0_INITIAL
		
		override fun serializeNBT() = NBTTagCompound().apply {
			setString("Stage", stage.key)
		}
		
		override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
			stage = CausatumStage.fromKey(getString("Stage")) ?: stage
		}
		
		class Provider : CapabilityProvider<CausatumCapability, NBTTagCompound>(CAP_CAUSATUM!!, CausatumCapability())
	}
}
