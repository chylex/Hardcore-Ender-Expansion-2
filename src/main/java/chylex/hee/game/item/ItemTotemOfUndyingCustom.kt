package chylex.hee.game.item

import chylex.hee.client.MC
import chylex.hee.game.entity.REACH_DISTANCE
import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.selectExistingEntities
import chylex.hee.game.inventory.heeTag
import chylex.hee.game.inventory.heeTagOrNull
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.game.potion.makeEffect
import chylex.hee.game.world.playClient
import chylex.hee.game.world.spawn
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.system.compatibility.MinecraftForgeEventBus
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntityVillager
import chylex.hee.system.migration.Potions
import chylex.hee.system.migration.Sounds
import chylex.hee.system.serialization.hasKey
import net.minecraft.entity.Entity
import net.minecraft.item.IItemPropertyGetter
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.particles.ParticleTypes
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.DamageSource
import net.minecraft.util.Hand
import net.minecraft.world.World
import net.minecraftforge.event.entity.living.LivingDeathEvent

class ItemTotemOfUndyingCustom(properties: Properties) : ItemAbstractTrinket(properties) {
	companion object {
		private const val SHAKING_TAG = "Shaking"
		
		val IS_SHAKING_PROPERTY = IItemPropertyGetter { stack, _, _ ->
			if (stack.heeTagOrNull.hasKey(SHAKING_TAG)) 1F else 0F
		}
	}
	
	init {
		MinecraftForgeEventBus.register(this)
	}
	
	override fun getTranslationKey(): String {
		return Items.TOTEM_OF_UNDYING.translationKey
	}
	
	// Trinket handling
	
	override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean {
		return !stack.isDamaged
	}
	
	@Sided(Side.CLIENT)
	override fun spawnClientTrinketBreakFX(target: Entity) {
		MC.particleManager.emitParticleAtEntity(target, ParticleTypes.TOTEM_OF_UNDYING, 30)
		Sounds.ITEM_TOTEM_USE.playClient(target.posVec, target.soundCategory)
	}
	
	// Death logic
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onLivingDeath(e: LivingDeathEvent) {
		if (e.source.canHarmInCreative()) {
			return
		}
		
		val player = e.entity as? EntityPlayer ?: return
		val trinketHandler = TrinketHandler.get(player)
		
		if (e.source == DamageSource.FALL && trinketHandler.isItemActive(ModItems.SCALE_OF_FREEFALL)) {
			return
		}
		
		trinketHandler.transformIfActive(this) {
			it.damage = it.maxDamage
			
			player.health = 1F
			player.clearActivePotions()
			player.addPotionEffect(Potions.REGENERATION.makeEffect(900, 1))
			player.addPotionEffect(Potions.ABSORPTION.makeEffect(100, 1))
			e.isCanceled = true
		}
	}
	
	// Villager logic
	
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean) {
		if (world.isRemote || world.totalTime % 10L != 0L || canPlaceIntoTrinketSlot(stack) || entity !is EntityPlayer) {
			return
		}
		
		val isNearVillager = world.selectExistingEntities.inRange<EntityVillager>(entity.posVec, entity.getAttributeValue(REACH_DISTANCE)).isNotEmpty()
		val wasNearVillager = stack.heeTagOrNull.hasKey(SHAKING_TAG)
		
		if (isNearVillager && !wasNearVillager) {
			stack.heeTag.putBoolean(SHAKING_TAG, true)
		}
		else if (!isNearVillager && wasNearVillager) {
			stack.heeTag.remove(SHAKING_TAG)
		}
	}
	
	override fun itemInteractionForEntity(stack: ItemStack, player: EntityPlayer, target: EntityLivingBase, hand: Hand): ActionResultType {
		if (target !is EntityVillager || !player.isSneaking || canPlaceIntoTrinketSlot(stack)) {
			return FAIL
		}
		
		val world = target.world
		
		if (world.isRemote) {
			return SUCCESS
		}
		
		stack.damage -= if (target.isChild) 2 else 1
		stack.heeTagOrNull?.remove(SHAKING_TAG)
		
		player.setHeldItem(hand, stack)
		player.cooldownTracker.setCooldown(this, 64)
		
		world.spawn(ModEntities.VILLAGER_DYING) {
			copyLocationAndAnglesFrom(target)
			copyVillagerDataFrom(target)
			// POLISH improve freezing during movement
		}
		
		target.remove()
		return SUCCESS
	}
	
	// Client side
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean {
		return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
}
