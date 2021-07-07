package chylex.hee.game.item

import chylex.hee.client.util.MC
import chylex.hee.game.Resource
import chylex.hee.game.entity.util.REACH_DISTANCE
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectExistingEntities
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.item.util.ItemProperty
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.game.potion.util.makeInstance
import chylex.hee.game.world.util.spawn
import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.system.MinecraftForgeEventBus
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.nbt.hasKey
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.merchant.villager.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.particles.ParticleTypes
import net.minecraft.potion.Effects
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.DamageSource
import net.minecraft.util.Hand
import net.minecraft.util.SoundEvents
import net.minecraft.world.World
import net.minecraftforge.event.entity.living.LivingDeathEvent

class ItemTotemOfUndyingCustom(properties: Properties) : ItemAbstractTrinket(properties) {
	companion object {
		private const val SHAKING_TAG = "Shaking"
		
		val IS_SHAKING_PROPERTY = ItemProperty(Resource.Custom("is_shaking")) { stack ->
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
		SoundEvents.ITEM_TOTEM_USE.playClient(target.posVec, target.soundCategory)
	}
	
	// Death logic
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onLivingDeath(e: LivingDeathEvent) {
		if (e.source.canHarmInCreative()) {
			return
		}
		
		val player = e.entity as? PlayerEntity ?: return
		val trinketHandler = TrinketHandler.get(player)
		
		if (e.source == DamageSource.FALL && trinketHandler.isItemActive(ModItems.SCALE_OF_FREEFALL)) {
			return
		}
		
		trinketHandler.transformIfActive(this) {
			it.damage = it.maxDamage
			
			player.health = 1F
			player.clearActivePotions()
			player.addPotionEffect(Effects.REGENERATION.makeInstance(900, 1))
			player.addPotionEffect(Effects.ABSORPTION.makeInstance(100, 1))
			e.isCanceled = true
		}
	}
	
	// Villager logic
	
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean) {
		if (world.isRemote || world.gameTime % 10L != 0L || canPlaceIntoTrinketSlot(stack) || entity !is PlayerEntity) {
			return
		}
		
		val isNearVillager = world.selectExistingEntities.inRange<VillagerEntity>(entity.posVec, entity.getAttributeValue(REACH_DISTANCE)).isNotEmpty()
		val wasNearVillager = stack.heeTagOrNull.hasKey(SHAKING_TAG)
		
		if (isNearVillager && !wasNearVillager) {
			stack.heeTag.putBoolean(SHAKING_TAG, true)
		}
		else if (!isNearVillager && wasNearVillager) {
			stack.heeTag.remove(SHAKING_TAG)
		}
	}
	
	override fun itemInteractionForEntity(stack: ItemStack, player: PlayerEntity, target: LivingEntity, hand: Hand): ActionResultType {
		if (target !is VillagerEntity || !player.isSneaking || canPlaceIntoTrinketSlot(stack)) {
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
