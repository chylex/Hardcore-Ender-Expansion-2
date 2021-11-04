package chylex.hee.game.item

import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectVulnerableEntities
import chylex.hee.game.entity.util.setFireTicks
import chylex.hee.game.fx.FxEntityData
import chylex.hee.game.fx.util.playUniversal
import chylex.hee.game.item.ItemScorchingSword.HitStats.GENERAL
import chylex.hee.game.item.ItemScorchingSword.HitStats.IMMUNE
import chylex.hee.game.item.ItemScorchingSword.HitStats.PASSIVE
import chylex.hee.game.item.properties.CustomToolMaterial.SCORCHING_SWORD
import chylex.hee.game.item.repair.ICustomRepairBehavior
import chylex.hee.game.item.util.doDamage
import chylex.hee.game.mechanics.scorching.IScorchingItem
import chylex.hee.game.mechanics.scorching.ScorchingHelper
import chylex.hee.game.mechanics.scorching.ScorchingHelper.FX_ENTITY_HIT
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.MinecraftForgeEventBus
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.square
import chylex.hee.util.math.toRadians
import chylex.hee.util.random.nextFloat
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE
import net.minecraft.entity.passive.AmbientEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.SquidEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.potion.Effects
import net.minecraft.util.DamageSource
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.SoundEvents
import net.minecraftforge.event.entity.living.LivingDamageEvent
import net.minecraftforge.event.entity.living.LootingLevelEvent
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class ItemScorchingSword(
	properties: Properties,
) : SwordItem(SCORCHING_SWORD, 0, -2.4F, properties), IHeeItem, IScorchingItem, ICustomRepairBehavior by ScorchingHelper.Repair(SCORCHING_SWORD) {
	override val material
		get() = SCORCHING_SWORD
	
	private val stopDamageRecursion = ThreadLocal.withInitial { false }
	
	init {
		MinecraftForgeEventBus.register(this)
	}
	
	private fun validateDamageSource(source: DamageSource): Pair<LivingEntity, ItemStack>? {
		val entity = source.trueSource as? LivingEntity ?: return null
		val heldItem = entity.getHeldItem(MAIN_HAND).takeIf { it.item === this } ?: return null
		
		return entity to heldItem
	}
	
	// Mining behavior
	
	override fun canMine(state: BlockState): Boolean {
		return false
	}
	
	// Hitting behavior
	
	override fun hitEntity(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
		target.setFire(getTargetType(target).fire)
		PacketClientFX(FX_ENTITY_HIT, FxEntityData(target)).sendToAllAround(target, 32.0)
		
		stack.doDamage(1, attacker, MAIN_HAND)
		return true
	}
	
	fun handleDamageMultiplier(target: LivingEntity): Float {
		val type = getTargetType(target)
		
		target.setFireTicks(type.fire) // set fire before drops
		return type.damageMultiplier
	}
	
	@SubscribeEvent(EventPriority.LOWEST)
	fun onLivingDamage(e: LivingDamageEvent) {
		val (attacker, heldItem) = e.source.takeUnless { stopDamageRecursion.get() }?.let(::validateDamageSource) ?: return
		val target = e.entityLiving
		
		val type = getTargetType(target)
		
		if (type.fire == 0 || e.amount < type.damage * 0.9F) { // somewhat prevents uncharged attacks
			return
		}
		
		val rand = attacker.rng
		val yaw = attacker.rotationYaw.toDouble()
		
		val sweepDamage = e.amount / 3F
		var totalHits = 0
		
		stopDamageRecursion.set(true)
		
		for (entity in target.world.selectVulnerableEntities.inBox<LivingEntity>(target.boundingBox.grow(1.0, 0.25, 1.0))) {
			if (entity !== attacker && entity !== target && !attacker.isOnSameTeam(entity) && getTargetType(entity).fire > 0 && attacker.getDistanceSq(entity) <= square(3)) {
				val source = if (attacker is PlayerEntity)
					DamageSource.causePlayerDamage(attacker)
				else
					DamageSource.causeMobDamage(attacker)
				
				entity.applyKnockback(0.4F, sin(yaw.toRadians()), -cos(yaw.toRadians()))
				entity.setFireTicks((type.fire / rand.nextFloat(1.6F, 2.4F)).floorToInt())
				entity.attackEntityFrom(source, sweepDamage)
				
				++totalHits
			}
		}
		
		stopDamageRecursion.set(false)
		
		if (attacker is PlayerEntity) {
			SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP.playUniversal(attacker, attacker.posVec, attacker.soundCategory)
			attacker.spawnSweepParticles()
		}
		
		val totalDamage = totalHits / 3
		
		if (totalDamage > 0) {
			heldItem.doDamage(totalDamage, attacker, MAIN_HAND)
		}
	}
	
	@SubscribeEvent(EventPriority.LOW)
	fun onMobDrops(e: LootingLevelEvent) {
		if (validateDamageSource(e.damageSource) != null) {
			e.lootingLevel = max(e.lootingLevel, getTargetType(e.entityLiving).looting)
		}
	}
	
	// Hitting statistics
	
	private fun isImmune(target: LivingEntity): Boolean {
		return target.isImmuneToFire || target.isPotionActive(Effects.FIRE_RESISTANCE)
	}
	
	private fun isPassive(target: LivingEntity): Boolean {
		return !target.attributeManager.let { it.hasAttributeInstance(ATTACK_DAMAGE) && it.getAttributeBaseValue(ATTACK_DAMAGE) > 0.0 } && (target is AnimalEntity || target is AmbientEntity || target is SquidEntity)
	}
	
	private fun getTargetType(target: LivingEntity) = when {
		isImmune(target)  -> IMMUNE
		isPassive(target) -> PASSIVE
		else              -> GENERAL
	}
	
	private enum class HitStats(val damage: Int, val fire: Int, val looting: Int) {
		IMMUNE (damage =  3, fire =   0, looting = 0),
		GENERAL(damage =  9, fire = 120, looting = 1),
		PASSIVE(damage = 15, fire = 120, looting = 2);
		
		val damageMultiplier
			get() = damage / 9F
	}
	
	// Repair handling
	
	override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean {
		return ScorchingHelper.onGetIsRepairable(this, repairWith)
	}
}
