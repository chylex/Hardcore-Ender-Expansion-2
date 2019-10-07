package chylex.hee.game.item
import chylex.hee.client.util.MC
import chylex.hee.game.entity.living.EntityMobVillagerDying
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.init.ModItems
import chylex.hee.system.Resource
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectExistingEntities
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayer.REACH_DISTANCE
import net.minecraft.init.MobEffects.ABSORPTION
import net.minecraft.init.MobEffects.REGENERATION
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumParticleTypes
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingDeathEvent

class ItemTotemOfUndyingCustom : ItemAbstractTrinket(){
	private companion object{
		private const val SHAKING_TAG = "Shaking"
	}
	
	init{
		maxDamage = 4
		
		addPropertyOverride(Resource.Custom("is_shaking")){
			stack, _, _ -> if (stack.heeTagOrNull.hasKey(SHAKING_TAG)) 1F else 0F
		}
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	// Trinket handling
	
	override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean{
		return !stack.isItemDamaged
	}
	
	@Sided(Side.CLIENT)
	override fun spawnClientTrinketBreakFX(target: Entity){
		MC.particleManager.emitParticleAtEntity(target, EnumParticleTypes.TOTEM, 30)
		SoundEvents.ITEM_TOTEM_USE.playClient(target.posVec, target.soundCategory)
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
			it.itemDamage = it.maxDamage
			
			player.health = 1F
			player.clearActivePotions()
			player.addPotionEffect(PotionEffect(REGENERATION, 900, 1))
			player.addPotionEffect(PotionEffect(ABSORPTION, 100, 1))
			e.isCanceled = true
		}
	}
	
	// Villager logic
	
	override fun onUpdate(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		if (world.isRemote || world.totalWorldTime % 10L != 0L || canPlaceIntoTrinketSlot(stack) || entity !is EntityPlayer){
			return
		}
		
		val isNearVillager = world.selectExistingEntities.inRange<EntityVillager>(entity.posVec, entity.getEntityAttribute(REACH_DISTANCE).attributeValue).isNotEmpty()
		val wasNearVillager = stack.heeTagOrNull.hasKey(SHAKING_TAG)
		
		if (isNearVillager && !wasNearVillager){
			stack.heeTag.setBoolean(SHAKING_TAG, true)
		}
		else if (!isNearVillager && wasNearVillager){
			stack.heeTag.removeTag(SHAKING_TAG)
		}
	}
	
	override fun itemInteractionForEntity(stack: ItemStack, player: EntityPlayer, target: EntityLivingBase, hand: EnumHand): Boolean{
		if (target !is EntityVillager || !player.isSneaking || canPlaceIntoTrinketSlot(stack)){
			return false
		}
		
		val world = target.world
		
		if (world.isRemote){
			return true
		}
		
		stack.itemDamage -= if (target.isChild) 1 else 2
		stack.heeTagOrNull?.removeTag(SHAKING_TAG)
		
		player.setHeldItem(hand, stack)
		player.cooldownTracker.setCooldown(this, 64)
		
		EntityMobVillagerDying(world).apply {
			copyLocationAndAnglesFrom(target)
			copyVillagerDataFrom(target)
			world.spawnEntity(this)
		}
		
		target.setDead()
		return true
	}
	
	// Client side
	
	override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean{
		return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
	}
}
