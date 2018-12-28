package chylex.hee.game.mechanics.instability
import chylex.hee.game.mechanics.instability.Instability.InstabilityCapability.Provider
import chylex.hee.game.mechanics.instability.dimension.DimensionInstabilityNull
import chylex.hee.game.mechanics.instability.dimension.IDimensionInstability
import chylex.hee.game.mechanics.instability.region.RegionInstability
import chylex.hee.system.Resource
import chylex.hee.system.util.forge.capabilities.CapabilityProvider
import chylex.hee.system.util.forge.capabilities.NullFactory
import chylex.hee.system.util.forge.capabilities.NullStorage
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraft.world.WorldProviderSurface
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase.START
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent

object Instability{
	fun register(){
		CapabilityManager.INSTANCE.register(InstabilityCapability::class.java, NullStorage.get(), NullFactory.get())
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	fun get(world: World): IDimensionInstability{
		return world.getCapability(CAP_INSTABILITY!!, null)?.dimension ?: DimensionInstabilityNull
	}
	
	// World events
	
	@SubscribeEvent
	fun onWorldTick(e: WorldTickEvent){
		if (e.phase == START){
			e.world.getCapability(CAP_INSTABILITY!!, null)?.region?.update()
		}
	}
	
	// Capability handling
	
	private val CAP_KEY = Resource.Custom("instability")
	
	@JvmStatic
	@CapabilityInject(InstabilityCapability::class)
	private var CAP_INSTABILITY: Capability<InstabilityCapability>? = null
	
	@SubscribeEvent
	fun onAttachCapabilities(e: AttachCapabilitiesEvent<World>){
		val world = e.`object`
		
		when(world.provider){
			// TODO
		}
	}
	
	private class InstabilityCapability private constructor(val dimension: IDimensionInstability, val region: RegionInstability<*>) : INBTSerializable<NBTTagCompound>{
		override fun serializeNBT() = NBTTagCompound().apply {
			setTag("Dimension", dimension.serializeNBT())
			setTag("Region", region.serializeNBT())
		}
		
		override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
			dimension.deserializeNBT(getCompoundTag("Dimension"))
			region.deserializeNBT(getCompoundTag("Region"))
		}
		
		class Provider(dimension: IDimensionInstability, region: RegionInstability<*>) : CapabilityProvider<InstabilityCapability, NBTTagCompound>(CAP_INSTABILITY!!, InstabilityCapability(dimension, region))
	}
}
