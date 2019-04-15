package chylex.hee.game.world.territory.storage
import chylex.hee.game.world.territory.storage.TokenPlayerStorage.TokenStorageCapability.Provider
import chylex.hee.init.ModItems
import chylex.hee.system.Resource
import chylex.hee.system.capability.CapabilityProvider
import chylex.hee.system.capability.PlayerCapabilityHandler
import chylex.hee.system.capability.PlayerCapabilityHandler.IPlayerPersistentCapability
import chylex.hee.system.util.getCap
import chylex.hee.system.util.register
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.items.ItemStackHandler

object TokenPlayerStorage{
	fun register(){
		CapabilityManager.INSTANCE.register<TokenStorageCapability>()
		PlayerCapabilityHandler.register(Handler)
	}
	
	fun forPlayer(player: EntityPlayer): ItemStackHandler{
		return Handler.retrieve(player)
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
	
	private class TokenStorageCapability private constructor() : ItemStackHandler(5 * 9){
		override fun setSize(size: Int){
			if (size != slots){
				throw IllegalArgumentException("cannot resize TokenStorageCapability")
			}
		}
		
		override fun isItemValid(slot: Int, stack: ItemStack): Boolean{
			return stack.item === ModItems.PORTAL_TOKEN
		}
		
		override fun getSlotLimit(slot: Int): Int{
			return 1
		}
		
		class Provider : CapabilityProvider<TokenStorageCapability, NBTTagCompound>(CAP_TOKEN_STORAGE, TokenStorageCapability())
	}
}
