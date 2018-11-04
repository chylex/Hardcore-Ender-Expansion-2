package chylex.hee.game.item
import chylex.hee.game.item.base.ItemBaseTrinket
import chylex.hee.game.mechanics.TrinketHandler
import chylex.hee.system.util.copyIf
import chylex.hee.system.util.isNotEmpty
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.EnumHand.OFF_HAND
import net.minecraft.util.NonNullList
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AnvilUpdateEvent
import net.minecraftforge.event.entity.living.LivingDamageEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.living.LivingHurtEvent
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST
import net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.UUID

class ItemRingOfPreservation : ItemBaseTrinket(){
	private class HurtPlayerInfo private constructor(val playerId: UUID, val worldTime: Long){
		constructor(player: EntityPlayer) : this(player.uniqueID, player.world.totalWorldTime)
		
		fun matches(player: EntityPlayer): Boolean{
			return player.uniqueID == playerId && player.world.totalWorldTime == worldTime
		}
	}
	
	private var lastHurtPlayerArmor: Pair<HurtPlayerInfo, Array<ItemStack>>? = null
	
	init{
		maxDamage = 1
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	// Trinket handling
	
	override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean{
		return !stack.isItemDamaged
	}
	
	private fun isTrinketActive(trinketItem: ItemStack): Boolean{
		return trinketItem.item === this && canPlaceIntoTrinketSlot(trinketItem)
	}
	
	private fun onItemDestroyed(player: EntityPlayer, stack: ItemStack, info: Pair<NonNullList<ItemStack>, Int>){
		if (!stack.isItemStackDamageable){
			return
		}
		
		val trinketItem = TrinketHandler.getCurrentItem(player)
		
		if (!isTrinketActive(trinketItem)){
			return
		}
		
		val (inventory, slot) = info
		inventory[slot] = stack.also { it.itemDamage = (it.maxDamage * 4) / 5 }
		
		trinketItem.itemDamage = 1
	}
	
	// Repair handling
	
	override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean{
		return toRepair.isItemDamaged && repairWith.item === Items.DIAMOND
	}
	
	@SubscribeEvent
	fun onAnvilUpdate(e: AnvilUpdateEvent){
		if (e.left.item === this && e.right.item === Items.DIAMOND){
			e.output = e.left.copy().also { it.itemDamage = 0 }
			e.cost = 4
		}
	}
	
	// Item destruction handling
	
	private inline fun getServerEventPlayer(e: LivingEvent): EntityPlayer? =
		(e.entity as? EntityPlayer)?.takeUnless { it.world.isRemote }
	
	private inline fun getServerEventPlayer(e: PlayerEvent): EntityPlayer? =
		e.entityPlayer.takeUnless { it.world.isRemote }
	
	// UPDATE: check if EntityLivingBase.damageEntity still triggers the events in the same order to allow detecting armor breaking, or if PlayerDestroyItemEvent gets fixed
	
	@SubscribeEvent(priority = HIGHEST)
	fun onPlayerDestroyItem(e: PlayerDestroyItemEvent){
		val player = getServerEventPlayer(e) ?: return
		val inventory = player.inventory
		
		val info = when(e.hand){
			MAIN_HAND -> Pair(inventory.mainInventory, inventory.currentItem)
			OFF_HAND  -> Pair(inventory.offHandInventory, 1)
			else      -> return // called for crafting with containers
		}
		
		onItemDestroyed(player, e.original, info)
	}
	
	@SubscribeEvent(priority = LOWEST)
	fun onLivingHurt(e: LivingHurtEvent){
		val player = getServerEventPlayer(e)
		
		if (player == null || !isTrinketActive(TrinketHandler.getCurrentItem(player))){
			return
		}
		
		val armorInventory = player.inventory.armorInventory
		
		val armorCopy = Array(armorInventory.size){
			index -> armorInventory[index].copyIf { it.isNotEmpty }
		}
		
		lastHurtPlayerArmor = Pair(HurtPlayerInfo(player), armorCopy)
	}
	
	@SubscribeEvent(priority = HIGHEST, receiveCanceled = true)
	fun onLivingDamage(e: LivingDamageEvent){
		val (hurtInfo, prevArmor) = lastHurtPlayerArmor ?: return
		val player = getServerEventPlayer(e) ?: return
		
		if (hurtInfo.matches(player)){
			val armorInventory = player.inventory.armorInventory
			
			for((slot, prevStack) in prevArmor.withIndex()){
				if (prevStack.isNotEmpty && armorInventory[slot].isEmpty){
					onItemDestroyed(player, prevStack, Pair(armorInventory, slot))
					break
				}
			}
		}
		
		lastHurtPlayerArmor = null
	}
}
