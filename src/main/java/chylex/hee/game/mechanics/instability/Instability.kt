package chylex.hee.game.mechanics.instability

import chylex.hee.game.Resource
import chylex.hee.game.mechanics.instability.Instability.InstabilityCapability.Provider
import chylex.hee.game.mechanics.instability.dimension.DimensionInstabilityEndTerritory
import chylex.hee.game.mechanics.instability.dimension.DimensionInstabilityGlobal
import chylex.hee.game.mechanics.instability.dimension.DimensionInstabilityNull
import chylex.hee.game.mechanics.instability.dimension.IDimensionInstability
import chylex.hee.game.mechanics.instability.dimension.components.EndermiteSpawnLogicOverworld
import chylex.hee.game.mechanics.instability.region.RegionInstability
import chylex.hee.game.mechanics.instability.region.entry.Entry5x5
import chylex.hee.game.world.isEndDimension
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.forge.capability.CapabilityProvider
import chylex.hee.util.forge.capability.getCapOrNull
import chylex.hee.util.forge.capability.register
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.TickEvent.Phase
import net.minecraftforge.event.TickEvent.WorldTickEvent

object Instability {
	fun register() {
		CapabilityManager.INSTANCE.register<InstabilityCapability>()
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	fun get(world: World): IDimensionInstability {
		return world.getCapOrNull(CAP_INSTABILITY)?.dimension ?: DimensionInstabilityNull
	}
	
	// World events
	
	@SubscribeEvent
	fun onWorldTick(e: WorldTickEvent) {
		if (e.phase == Phase.START) {
			e.world.getCapOrNull(CAP_INSTABILITY)?.region?.update()
		}
	}
	
	// Capability handling
	
	private val CAP_KEY = Resource.Custom("instability")
	
	private const val DIMENSION_TAG = "Dimension"
	private const val REGION_TAG = "Region"
	
	@JvmStatic
	@CapabilityInject(InstabilityCapability::class)
	private var CAP_INSTABILITY: Capability<InstabilityCapability>? = null
	
	@SubscribeEvent
	fun onAttachCapabilities(e: AttachCapabilitiesEvent<World>) {
		val world = e.`object`
		
		if (world.isRemote) {
			return
		}
		
		if (world.isEndDimension) {
			e.addCapability(CAP_KEY, Provider(DimensionInstabilityEndTerritory(world), RegionInstability(world, Entry5x5.Constructor)))
		}
		else {
			e.addCapability(CAP_KEY, Provider(DimensionInstabilityGlobal(world, EndermiteSpawnLogicOverworld), RegionInstability(world, Entry5x5.Constructor)))
		}
	}
	
	private class InstabilityCapability private constructor(val dimension: IDimensionInstability, val region: RegionInstability<*>) : INBTSerializable<TagCompound> {
		override fun serializeNBT() = TagCompound().apply {
			put(DIMENSION_TAG, dimension.serializeNBT())
			put(REGION_TAG, region.serializeNBT())
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			dimension.deserializeNBT(getCompound(DIMENSION_TAG))
			region.deserializeNBT(getCompound(REGION_TAG))
		}
		
		class Provider(dimension: IDimensionInstability, region: RegionInstability<*>) : CapabilityProvider<InstabilityCapability, TagCompound>(CAP_INSTABILITY!!, InstabilityCapability(dimension, region))
	}
}
