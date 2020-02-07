package chylex.hee.game.item
import chylex.hee.client.util.MC
import chylex.hee.game.entity.living.EntityMobVillagerDying
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.init.ModItems
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.EntityVillager
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.compatibility.MinecraftForgeEventBus
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.makeEffect
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.totalTime
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity.REACH_DISTANCE
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.particles.ParticleTypes
import net.minecraft.util.DamageSource
import net.minecraft.util.Hand
import net.minecraft.world.World
import net.minecraftforge.event.entity.living.LivingDeathEvent

class ItemTotemOfUndyingCustom(properties: Properties) : ItemAbstractTrinket(properties){
	private companion object{
		private const val SHAKING_TAG = "Shaking"
	}
	
	init{
		addPropertyOverride(Resource.Custom("is_shaking")){
			stack, _, _ -> if (stack.heeTagOrNull.hasKey(SHAKING_TAG)) 1F else 0F
		}
		
		MinecraftForgeEventBus.register(this)
	}
	
	override fun getTranslationKey(): String{
		return Items.TOTEM_OF_UNDYING.translationKey
	}
	
	// Trinket handling
	
	override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean{
		return !stack.isDamaged
	}
	
	@Sided(Side.CLIENT)
	override fun spawnClientTrinketBreakFX(target: Entity){
		MC.particleManager.emitParticleAtEntity(target, ParticleTypes.TOTEM_OF_UNDYING, 30)
		Sounds.ITEM_TOTEM_USE.playClient(target.posVec, target.soundCategory)
	}
	
	// Death logic
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onLivingDeath(e: LivingDeathEvent){
		if (e.source.canHarmInCreative()){
			return
		}
		
		val player = e.entity as? EntityPlayer ?: return
		val trinketHandler = TrinketHandler.get(player)
		
		if (e.source == DamageSource.FALL && trinketHandler.isItemActive(ModItems.SCALE_OF_FREEFALL)){
			return
		}
		
		trinketHandler.transformIfActive(this){
			it.damage = it.maxDamage
			
			player.health = 1F
			player.clearActivePotions()
			player.addPotionEffect(Potions.REGENERATION.makeEffect(900, 1))
			player.addPotionEffect(Potions.ABSORPTION.makeEffect(100, 1))
			e.isCanceled = true
		}
	}
	
	// Villager logic
	
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		if (world.isRemote || world.totalTime % 10L != 0L || canPlaceIntoTrinketSlot(stack) || entity !is EntityPlayer){
			return
		}
		
		val isNearVillager = world.selectExistingEntities.inRange<EntityVillager>(entity.posVec, entity.getAttribute(REACH_DISTANCE).value).isNotEmpty()
		val wasNearVillager = stack.heeTagOrNull.hasKey(SHAKING_TAG)
		
		if (isNearVillager && !wasNearVillager){
			stack.heeTag.putBoolean(SHAKING_TAG, true)
		}
		else if (!isNearVillager && wasNearVillager){
			stack.heeTag.remove(SHAKING_TAG)
		}
	}
	
	override fun itemInteractionForEntity(stack: ItemStack, player: EntityPlayer, target: EntityLivingBase, hand: Hand): Boolean{
		if (target !is EntityVillager || !player.isSneaking || canPlaceIntoTrinketSlot(stack)){
			return false
		}
		
		val world = target.world
		
		if (world.isRemote){
			return true
		}
		
		stack.damage -= if (target.isChild) 1 else 2
		stack.heeTagOrNull?.remove(SHAKING_TAG)
		
		player.setHeldItem(hand, stack)
		player.cooldownTracker.setCooldown(this, 64)
		
		EntityMobVillagerDying(world).apply {
			copyLocationAndAnglesFrom(target)
			copyVillagerDataFrom(target)
			world.addEntity(this)
		}
		
		target.remove()
		return true
	}
	
	// Client side
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean{
		return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
}
