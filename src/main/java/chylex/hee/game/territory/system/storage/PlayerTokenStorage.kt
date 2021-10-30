package chylex.hee.game.territory.system.storage

import chylex.hee.game.Resource
import chylex.hee.game.entity.player.PlayerCapabilityHandler
import chylex.hee.game.entity.player.PlayerCapabilityHandler.IPlayerPersistentCapability
import chylex.hee.game.item.ItemPortalToken
import chylex.hee.game.territory.system.storage.PlayerTokenStorage.TokenStorageCapability.Provider
import chylex.hee.init.ModItems
import chylex.hee.util.forge.capability.CapabilityProvider
import chylex.hee.util.forge.capability.getCap
import chylex.hee.util.forge.capability.register
import chylex.hee.util.nbt.TagCompound
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.items.ItemStackHandler

object PlayerTokenStorage {
	const val ROWS = 5
	
	fun register() {
		CapabilityManager.INSTANCE.register<TokenStorageCapability>()
		PlayerCapabilityHandler.register(Handler)
	}
	
	fun forPlayer(player: PlayerEntity): ItemStackHandler {
		return Handler.retrieve(player).also {
			for (slot in 0 until it.slots) {
				it.getStackInSlot(slot).takeIf(::isStackValid)?.let(ItemPortalToken::updateCorruptedState)
			}
		}
	}
	
	private fun isStackValid(stack: ItemStack): Boolean {
		return stack.item === ModItems.PORTAL_TOKEN
	}
	
	// Capability handling
	
	private object Handler : IPlayerPersistentCapability<TokenStorageCapability> {
		override val key = Resource.Custom("tokens")
		override fun provide(player: PlayerEntity) = Provider()
		override fun retrieve(player: PlayerEntity) = player.getCap(CAP_TOKEN_STORAGE)
	}
	
	@JvmStatic
	@CapabilityInject(TokenStorageCapability::class)
	private var CAP_TOKEN_STORAGE: Capability<TokenStorageCapability>? = null
	
	private class TokenStorageCapability private constructor() : ItemStackHandler(9 * ROWS) {
		override fun setSize(size: Int) {
			require(size == slots) { "cannot resize TokenStorageCapability" }
		}
		
		override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
			return isStackValid(stack)
		}
		
		override fun getSlotLimit(slot: Int): Int {
			return 1
		}
		
		class Provider : CapabilityProvider<TokenStorageCapability, TagCompound>(CAP_TOKEN_STORAGE, TokenStorageCapability())
	}
}
