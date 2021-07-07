package chylex.hee.game.item

import chylex.hee.game.entity.util.posVec
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.item.repair.ICustomRepairBehavior
import chylex.hee.game.item.repair.RepairInstance
import chylex.hee.game.item.util.copyIfNotEmpty
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.init.ModSounds
import chylex.hee.system.MinecraftForgeEventBus
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.forge.SubscribeEvent
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.Hand.OFF_HAND
import net.minecraft.util.NonNullList
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent
import java.util.function.Consumer

class ItemRingOfPreservation(properties: Properties) : ItemAbstractTrinket(properties), ICustomRepairBehavior {
	init {
		MinecraftForgeEventBus.register(this)
	}
	
	// Trinket handling
	
	override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean {
		return !stack.isDamaged
	}
	
	@Sided(Side.CLIENT)
	override fun spawnClientTrinketBreakFX(target: Entity) {
		ModSounds.ITEM_RING_OF_PRESERVATION_USE.playClient(target.posVec, target.soundCategory, volume = 0.7F)
	}
	
	private fun onItemDestroyed(player: PlayerEntity, stack: ItemStack, info: Pair<NonNullList<ItemStack>, Int>) {
		if (!stack.isDamageable) {
			return
		}
		
		TrinketHandler.get(player).transformIfActive(this) {
			it.damage = 1
			
			val (inventory, slot) = info
			inventory[slot] = stack.apply { damage = (maxDamage * 4) / 5 }
		}
	}
	
	// Repair handling
	
	override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean {
		return repairWith.item === Items.DIAMOND
	}
	
	override fun onRepairUpdate(instance: RepairInstance) = with(instance) {
		repairFully()
		experienceCost = 4
	}
	
	// Item destruction handling
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onPlayerDestroyItem(e: PlayerDestroyItemEvent) {
		val player = e.player
		val inventory = player.inventory
		
		val info = when (e.hand) {
			MAIN_HAND -> Pair(inventory.mainInventory, inventory.currentItem)
			OFF_HAND  -> Pair(inventory.offHandInventory, 1)
			else      -> return // called for crafting with containers
		}
		
		onItemDestroyed(player, e.original, info)
	}
	
	fun onArmorDestroyed(player: PlayerEntity, originalStack: ItemStack, destroyedStack: ItemStack) {
		val armorInventory = player.inventory.armorInventory
		val armorIndex = armorInventory.indexOfFirst { it === destroyedStack }
		
		if (armorIndex != -1) {
			onItemDestroyed(player, originalStack, armorInventory to armorIndex)
		}
	}
	
	fun handleArmorDamage(entity: LivingEntity, stack: ItemStack, amount: Int, onBroken: Consumer<LivingEntity>) {
		val originalStack = stack.copyIfNotEmpty()
		stack.damageItem(amount, entity, onBroken)
		
		if (!originalStack.isEmpty && stack.isEmpty && entity is PlayerEntity) {
			onArmorDestroyed(entity, originalStack, stack)
		}
	}
}
