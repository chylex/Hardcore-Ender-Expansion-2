package chylex.hee.game.world.territory.storage
import chylex.hee.game.inventory.isNotEmpty
import chylex.hee.game.item.ItemPortalToken
import chylex.hee.game.world.territory.storage.TokenPlayerStorage.TokenStorageCapability.Provider
import chylex.hee.init.ModItems
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.capability.CapabilityProvider
import chylex.hee.system.forge.capability.PlayerCapabilityHandler
import chylex.hee.system.forge.capability.PlayerCapabilityHandler.IPlayerPersistentCapability
import chylex.hee.system.forge.capability.getCap
import chylex.hee.system.forge.capability.register
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.serialization.TagCompound
import net.minecraft.item.ItemStack
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.items.ItemStackHandler

object TokenPlayerStorage{
	const val ROWS = 5
	
	fun register(){
		CapabilityManager.INSTANCE.register<TokenStorageCapability>()
		PlayerCapabilityHandler.register(Handler)
	}
	
	fun forPlayer(player: EntityPlayer): ItemStackHandler{
		return Handler.retrieve(player).also {
			for(slot in 0 until it.slots){
				val stack = it.getStackInSlot(slot).takeIf { it.isNotEmpty } ?: continue
				val token = stack.item as? ItemPortalToken ?: continue
				
				token.updateCorruptedState(stack)
			}
		}
	}
	
	// Capability handling
	
	private object Handler : IPlayerPersistentCapability<TokenStorageCapability>{
		override val key = Resource.Custom("tokens")
		override fun provide(player: EntityPlayer) = Provider()
		override fun retrieve(player: EntityPlayer) = player.getCap(CAP_TOKEN_STORAGE)
	}
	
	@JvmStatic
	@CapabilityInject(TokenStorageCapability::class)
	private var CAP_TOKEN_STORAGE: Capability<TokenStorageCapability>? = null
	
	private class TokenStorageCapability private constructor() : ItemStackHandler(9 * ROWS){
		override fun setSize(size: Int){
			require(size == slots){ "cannot resize TokenStorageCapability" }
		}
		
		override fun isItemValid(slot: Int, stack: ItemStack): Boolean{
			return stack.item === ModItems.PORTAL_TOKEN
		}
		
		override fun getSlotLimit(slot: Int): Int{
			return 1
		}
		
		class Provider : CapabilityProvider<TokenStorageCapability, TagCompound>(CAP_TOKEN_STORAGE, TokenStorageCapability())
	}
}
