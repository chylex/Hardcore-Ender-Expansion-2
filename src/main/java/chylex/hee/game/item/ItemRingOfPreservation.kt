package chylex.hee.game.item
import chylex.hee.game.item.repair.ICustomRepairBehavior
import chylex.hee.game.item.repair.RepairInstance
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.Hand.OFF_HAND
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.copyIf
import chylex.hee.system.util.isNotEmpty
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingDamageEvent
import net.minecraftforge.event.entity.living.LivingHurtEvent
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent
import java.util.UUID

class ItemRingOfPreservation : ItemAbstractTrinket(), ICustomRepairBehavior{
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
	
	@Sided(Side.CLIENT)
	override fun spawnClientTrinketBreakFX(target: Entity){
		// TODO sound effect
	}
	
	private fun onItemDestroyed(player: EntityPlayer, stack: ItemStack, info: Pair<NonNullList<ItemStack>, Int>){
		if (!stack.isItemStackDamageable){
			return
		}
		
		TrinketHandler.get(player).transformIfActive(this){
			it.itemDamage = 1
			
			val (inventory, slot) = info
			inventory[slot] = stack.apply { itemDamage = (maxDamage * 4) / 5 }
		}
	}
	
	// Repair handling
	
	override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean{
		return toRepair.isItemDamaged && repairWith.item === Items.DIAMOND
	}
	
	override fun onRepairUpdate(instance: RepairInstance) = with(instance){
		repairFully()
		experienceCost = 4
	}
	
	// Item destruction handling
	
	// UPDATE: check if EntityLivingBase.damageEntity still triggers the events in the same order to allow detecting armor breaking, or if PlayerDestroyItemEvent gets fixed
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onPlayerDestroyItem(e: PlayerDestroyItemEvent){
		val player = e.entityPlayer
		val inventory = player.inventory
		
		val info = when(e.hand){
			MAIN_HAND -> Pair(inventory.mainInventory, inventory.currentItem)
			OFF_HAND  -> Pair(inventory.offHandInventory, 1)
			else      -> return // called for crafting with containers
		}
		
		onItemDestroyed(player, e.original, info)
	}
	
	@SubscribeEvent(EventPriority.LOWEST)
	fun onLivingHurt(e: LivingHurtEvent){
		val player = e.entity as? EntityPlayer
		
		if (player == null || !TrinketHandler.get(player).isItemActive(this)){
			return
		}
		
		val armorInventory = player.inventory.armorInventory
		
		val armorCopy = Array(armorInventory.size){
			index -> armorInventory[index].copyIf { it.isNotEmpty }
		}
		
		lastHurtPlayerArmor = Pair(HurtPlayerInfo(player), armorCopy)
	}
	
	@SubscribeEvent(EventPriority.HIGHEST, receiveCanceled = true)
	fun onLivingDamage(e: LivingDamageEvent){
		val (hurtInfo, prevArmor) = lastHurtPlayerArmor ?: return
		val player = e.entity as? EntityPlayer ?: return
		
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
