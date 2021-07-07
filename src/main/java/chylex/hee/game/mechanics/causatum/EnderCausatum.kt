package chylex.hee.game.mechanics.causatum

import chylex.hee.game.Resource
import chylex.hee.game.entity.player.PlayerCapabilityHandler
import chylex.hee.game.entity.player.PlayerCapabilityHandler.IPlayerPersistentCapability
import chylex.hee.game.mechanics.causatum.EnderCausatum.CausatumCapability.Provider
import chylex.hee.util.forge.capability.CapabilityProvider
import chylex.hee.util.forge.capability.getCap
import chylex.hee.util.forge.capability.register
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.entity.player.PlayerEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.util.INBTSerializable

object EnderCausatum {
	fun register() {
		CapabilityManager.INSTANCE.register<CausatumCapability>()
		PlayerCapabilityHandler.register(Handler)
	}
	
	fun getStage(player: PlayerEntity): CausatumStage {
		return Handler.retrieve(player).stage
	}
	
	fun triggerStage(player: PlayerEntity, newStage: CausatumStage, force: Boolean = false): Boolean {
		with(Handler.retrieve(player)) {
			if (newStage > stage || force) {
				stage = newStage
				return true
			}
		}
		
		return false
	}
	
	// Capability handling
	
	private object Handler : IPlayerPersistentCapability<CausatumCapability> {
		override val key = Resource.Custom("causatum")
		override fun provide(player: PlayerEntity) = Provider()
		override fun retrieve(player: PlayerEntity) = player.getCap(CAP_CAUSATUM)
	}
	
	private const val STAGE_TAG = "Stage"
	
	@JvmStatic
	@CapabilityInject(CausatumCapability::class)
	private var CAP_CAUSATUM: Capability<CausatumCapability>? = null
	
	private class CausatumCapability private constructor() : INBTSerializable<TagCompound> {
		var stage = CausatumStage.S0_INITIAL
		
		override fun serializeNBT() = TagCompound().apply {
			putString(STAGE_TAG, stage.key)
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			stage = CausatumStage.fromKey(getString(STAGE_TAG)) ?: stage
		}
		
		class Provider : CapabilityProvider<CausatumCapability, TagCompound>(CAP_CAUSATUM, CausatumCapability())
	}
}
