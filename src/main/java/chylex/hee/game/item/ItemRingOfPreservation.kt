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
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.compatibility.MinecraftForgeEventBus
import chylex.hee.system.util.copyIf
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.totalTime
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraftforge.event.entity.living.LivingDamageEvent
import net.minecraftforge.event.entity.living.LivingHurtEvent
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent
import java.util.UUID

class ItemRingOfPreservation(properties: Properties) : ItemAbstractTrinket(properties), ICustomRepairBehavior{
	private class HurtPlayerInfo private constructor(val playerId: UUID, val worldTime: Long){
		constructor(player: EntityPlayer) : this(player.uniqueID, player.world.totalTime)
		
		fun matches(player: EntityPlayer): Boolean{
			return player.uniqueID == playerId && player.world.totalTime == worldTime
		}
	}
	
	private var lastHurtPlayerArmor: Pair<HurtPlayerInfo, Array<ItemStack>>? = null
	
	init{
		MinecraftForgeEventBus.register(this)
	}
	
	// Trinket handling
	
	override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean{
		return !stack.isDamaged
	}
	
	@Sided(Side.CLIENT)
	override fun spawnClientTrinketBreakFX(target: Entity){
		// TODO sound effect
	}
	
	private fun onItemDestroyed(player: EntityPlayer, stack: ItemStack, info: Pair<NonNullList<ItemStack>, Int>){
		if (!stack.isDamageable){
			return
		}
		
		TrinketHandler.get(player).transformIfActive(this){
			it.damage = 1
			
			val (inventory, slot) = info
			inventory[slot] = stack.apply { damage = (maxDamage * 4) / 5 }
		}
	}
	
	// Repair handling
	
	override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean{
		return repairWith.item === Items.DIAMOND
	}
	
	override fun onRepairUpdate(instance: RepairInstance) = with(instance){
		repairFully()
		experienceCost = 4
	}
	
	// Item destruction handling
	
	// UPDATE 1.14 (PlayerDestroyItemEvent doesn't include armor, see if that changed or if EntityLivingBase.damageEntity still triggers events in the same order to allow detecting armor breaking)
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onPlayerDestroyItem(e: PlayerDestroyItemEvent){
		@Suppress("SENSELESS_COMPARISON")
		if (e.original == null){
			return // UPDATE 1.14 (the event is completely fucked and triggers with null stacks when placing blocks)
		}
		
		val player = e.player
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
