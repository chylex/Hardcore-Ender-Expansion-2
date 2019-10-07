package chylex.hee.game.mechanics.instability
import chylex.hee.game.mechanics.instability.Instability.InstabilityCapability.Provider
import chylex.hee.game.mechanics.instability.dimension.DimensionInstabilityEndTerritory
import chylex.hee.game.mechanics.instability.dimension.DimensionInstabilityGlobal
import chylex.hee.game.mechanics.instability.dimension.DimensionInstabilityNull
import chylex.hee.game.mechanics.instability.dimension.IDimensionInstability
import chylex.hee.game.mechanics.instability.dimension.components.EndermiteSpawnLogicOverworld
import chylex.hee.game.mechanics.instability.region.RegionInstability
import chylex.hee.game.mechanics.instability.region.entry.types.Entry5x5
import chylex.hee.system.Resource
import chylex.hee.system.capability.CapabilityProvider
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.getCapOrNull
import chylex.hee.system.util.register
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraft.world.WorldProviderEnd
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent

object Instability{
	fun register(){
		CapabilityManager.INSTANCE.register<InstabilityCapability>()
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	fun get(world: World): IDimensionInstability{
		return world.getCapOrNull(CAP_INSTABILITY)?.dimension ?: DimensionInstabilityNull
	}
	
	// World events
	
	@SubscribeEvent
	fun onWorldTick(e: WorldTickEvent){
		if (e.phase == Phase.START){
			e.world.getCapOrNull(CAP_INSTABILITY)?.region?.update()
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
			is WorldProviderEnd ->
				e.addCapability(CAP_KEY, Provider(DimensionInstabilityEndTerritory(world), RegionInstability(world, Entry5x5.Constructor)))
			
			else ->
				e.addCapability(CAP_KEY, Provider(DimensionInstabilityGlobal(world, EndermiteSpawnLogicOverworld), RegionInstability(world, Entry5x5.Constructor)))
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
