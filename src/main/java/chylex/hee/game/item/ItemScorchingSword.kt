package chylex.hee.game.item
import chylex.hee.game.fx.FxEntityData
import chylex.hee.game.item.ItemScorchingSword.HitStats.GENERAL
import chylex.hee.game.item.ItemScorchingSword.HitStats.IMMUNE
import chylex.hee.game.item.ItemScorchingSword.HitStats.PASSIVE
import chylex.hee.game.item.repair.ICustomRepairBehavior
import chylex.hee.game.item.util.CustomToolMaterial.SCORCHING_SWORD
import chylex.hee.game.mechanics.scorching.IScorchingItem
import chylex.hee.game.mechanics.scorching.ScorchingHelper
import chylex.hee.game.mechanics.scorching.ScorchingHelper.FX_ENTITY_HIT
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.EventResult
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getAttribute
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playUniversal
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.setFireTicks
import chylex.hee.system.util.square
import chylex.hee.system.util.toRadians
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.passive.EntityAmbientCreature
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.util.DamageSource
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingDamageEvent
import net.minecraftforge.event.entity.living.LootingLevelEvent
import net.minecraftforge.event.entity.player.CriticalHitEvent
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class ItemScorchingSword : ItemSword(SCORCHING_SWORD), IScorchingItem, ICustomRepairBehavior by ScorchingHelper.Repair(SCORCHING_SWORD){
	override val material: ToolMaterial
		get() = SCORCHING_SWORD
	
	private val stopDamageRecursion = ThreadLocal.withInitial { false }
	
	init{
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	private fun validateDamageSource(source: DamageSource): Pair<EntityLivingBase, ItemStack>?{
		val entity = source.trueSource as? EntityLivingBase ?: return null
		val heldItem = entity.getHeldItem(MAIN_HAND).takeIf { it.item === this } ?: return null
		
		return entity to heldItem
	}
	
	// Mining behavior
	
	override fun canMine(state: IBlockState): Boolean{
		return false
	}
	
	// Hitting behavior
	
	override fun hitEntity(stack: ItemStack, target: EntityLivingBase, attacker: EntityLivingBase): Boolean{
		target.setFire(getTargetType(target).fire)
		PacketClientFX(FX_ENTITY_HIT, FxEntityData(target)).sendToAllAround(target, 32.0)
		
		stack.damageItem(1, attacker)
		return true
	}
	
	override fun onHit(e: CriticalHitEvent){ // UPDATE abusing crit flag to disable sweep and modify damage, also doesn't work when held by a mob
		val target = e.target
		
		if (target !is EntityLivingBase){
			e.result = EventResult.DENY
			return
		}
		
		val type = getTargetType(target)
		
		target.setFireTicks(type.fire) // set fire before drops
		
		e.damageModifier = type.damageMultiplier
		e.result = EventResult.ALLOW // prevents sweep
	}
	
	@SubscribeEvent(EventPriority.LOWEST)
	fun onLivingDamage(e: LivingDamageEvent){
		val (attacker, heldItem) = e.source.takeUnless { stopDamageRecursion.get() }?.let(::validateDamageSource) ?: return
		val target = e.entityLiving
		
		val type = getTargetType(target)
		
		if (type.fire == 0 || e.amount < type.damage * 0.9F){ // somewhat prevents uncharged attacks
			return
		}
		
		val rand = attacker.rng
		val yaw = attacker.rotationYaw.toDouble()
		
		val sweepDamage = e.amount / 3F
		var totalHits = 0
		
		stopDamageRecursion.set(true)
		
		for(entity in target.world.selectVulnerableEntities.inBox<EntityLivingBase>(target.entityBoundingBox.grow(1.0, 0.25, 1.0))){
			if (entity !== attacker && entity !== target && !attacker.isOnSameTeam(entity) && getTargetType(entity).fire > 0 && attacker.getDistanceSq(entity) <= square(3)){
				val source = if (attacker is EntityPlayer)
					DamageSource.causePlayerDamage(attacker)
				else
					DamageSource.causeMobDamage(attacker)
				
				entity.knockBack(attacker, 0.4F, sin(yaw.toRadians()), -cos(yaw.toRadians()))
				entity.setFireTicks((type.fire / rand.nextFloat(1.6F, 2.4F)).floorToInt())
				entity.attackEntityFrom(source, sweepDamage)
				
				++totalHits
			}
		}
		
		stopDamageRecursion.set(false)
		
		if (attacker is EntityPlayer){
			Sounds.ENTITY_PLAYER_ATTACK_SWEEP.playUniversal(attacker, attacker.posVec, attacker.soundCategory)
			attacker.spawnSweepParticles()
		}
		
		val totalDamage = totalHits / 3
		
		if (totalDamage > 0){
			heldItem.damageItem(totalDamage, attacker)
		}
	}
	
	@SubscribeEvent(EventPriority.LOW)
	fun onMobDrops(e: LootingLevelEvent){
		if (validateDamageSource(e.damageSource) != null){
			e.lootingLevel = max(e.lootingLevel, getTargetType(e.entityLiving).looting)
		}
	}
	
	// Hitting statistics
	
	private fun isImmune(target: EntityLivingBase): Boolean{
		return target.isImmuneToFire || target.isPotionActive(MobEffects.FIRE_RESISTANCE)
	}
	
	private fun isPassive(target: EntityLivingBase): Boolean{
		@Suppress("SENSELESS_COMPARISON")
		if (target.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null){
			return false
		}
		
		return target is EntityAnimal || target is EntityAmbientCreature || target is EntitySquid
	}
	
	private fun getTargetType(target: EntityLivingBase) = when{
		isImmune(target) -> IMMUNE
		isPassive(target) -> PASSIVE
		else -> GENERAL
	}
	
	private enum class HitStats(val damage: Int, val fire: Int, val looting: Int){
		IMMUNE (damage =  3, fire =   0, looting = 0),
		GENERAL(damage =  9, fire = 120, looting = 1),
		PASSIVE(damage = 15, fire = 120, looting = 2);
		
		val damageMultiplier
			get() = damage / 9F
	}
	
	// Repair handling
	
	override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean{
		return ScorchingHelper.onGetIsRepairable(this, toRepair, repairWith)
	}
}
