package chylex.hee.game.item

import chylex.hee.game.entity.posVec
import chylex.hee.game.mechanics.damage.DamageProperties
import chylex.hee.game.mechanics.damage.DamageType
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ENCHANTMENT_PROTECTION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.POTION_PROTECTION
import chylex.hee.game.mechanics.explosion.ExplosionBuilder
import chylex.hee.game.mechanics.trinket.TrinketHandler
import chylex.hee.game.world.totalTime
import chylex.hee.system.compatibility.MinecraftForgeEventBus
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.math.square
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS
import net.minecraft.item.ItemStack
import net.minecraft.util.CombatRules
import net.minecraft.util.Hand
import net.minecraft.world.Difficulty
import net.minecraft.world.Difficulty.EASY
import net.minecraft.world.Difficulty.HARD
import net.minecraft.world.Difficulty.NORMAL
import net.minecraft.world.Difficulty.PEACEFUL
import net.minecraft.world.Explosion
import net.minecraftforge.event.entity.living.LivingHurtEvent
import net.minecraftforge.event.world.ExplosionEvent
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

class ItemTalismanOfGriefing(properties: Properties) : ItemAbstractTrinket(properties) {
	private companion object {
		private val BLAST_DAMAGE_PROPERTIES = DamageProperties().apply {
			with(Writer()) {
				setAllowArmor()
				addType(DamageType.BLAST)
			}
		}.Reader()
		
		private fun getBlastDamageAfterCalculations(explosion: Explosion, player: EntityPlayer): Float {
			val pos = explosion.position
			val radius = explosion.size
			
			val distanceScaled = player.posVec.distanceTo(pos) / (radius * 2F)
			
			val blastPower = (1 - distanceScaled) * Explosion.getBlockDensity(pos, player)
			val explosionDamage = 1F + ((square(blastPower) + blastPower) * radius * 7).toInt()
			
			var finalDamage = explosionDamage
			
			finalDamage = CombatRules.getDamageAfterAbsorb(finalDamage, player.totalArmorValue.toFloat(), player.getAttributeValue(ARMOR_TOUGHNESS).toFloat())
			finalDamage = POTION_PROTECTION.modifyDamage(finalDamage, player, BLAST_DAMAGE_PROPERTIES)
			finalDamage = ENCHANTMENT_PROTECTION.modifyDamage(finalDamage, player, BLAST_DAMAGE_PROPERTIES)
			
			return finalDamage
		}
		
		private fun getNormalDifficultyEquivalentDamage(amount: Float, currentDifficulty: Difficulty) = when(currentDifficulty) {
			PEACEFUL -> 0F
			EASY     -> max(amount, (amount - 1F) * 2F)
			NORMAL   -> amount
			HARD     -> amount / 1.5F
		}
	}
	
	private val lastRepairMarkTime = ThreadLocal.withInitial { Long.MIN_VALUE }
	private val lastRepairMarkEntities = ThreadLocal.withInitial { HashSet<UUID>(4) }
	
	init {
		MinecraftForgeEventBus.register(this)
	}
	
	override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean {
		return stack.damage < stack.maxDamage
	}
	
	private fun markEntitiesForTalismanRepair(explosion: Explosion, entities: List<Entity>) {
		val currentTime = explosion.world.totalTime
		val recentlyExploded = lastRepairMarkEntities.get()
		
		if (lastRepairMarkTime.get() != currentTime) {
			lastRepairMarkTime.set(currentTime)
			recentlyExploded.clear()
		}
		
		for(entity in entities) {
			if (entity is EntityLivingBase) {
				recentlyExploded.add(entity.uniqueID)
			}
		}
	}
	
	@SubscribeEvent
	fun onExplosionDetonate(e: ExplosionEvent.Detonate) {
		val explosion = e.explosion
		val entities = e.affectedEntities
		val world = explosion.world
		
		if (world.isRemote || entities.isEmpty()) {
			return
		}
		
		val radius = explosion.size
		
		if (radius >= 6F) {
			markEntitiesForTalismanRepair(explosion, entities)
		}
		
		val source = explosion.explosivePlacedBy
		
		if (source == null || source is EntityPlayer) { // TODO large fireballs don't set explosion source
			return
		}
		
		val diameter = radius * 2F
		val position = explosion.position
		
		for(entity in entities) {
			if (entity is EntityPlayer && !entity.isImmuneToExplosions && entity.posVec.distanceTo(position) <= diameter) {
				val trinketHandler = TrinketHandler.get(entity)
				
				if (trinketHandler.isItemActive(this)) {
					val finalDamage = getBlastDamageAfterCalculations(explosion, entity)
					val durabilityTaken = (finalDamage / 10F).ceilToInt().coerceAtMost(3)
					
					trinketHandler.transformIfActive(this) {
						it.damage = min(it.maxDamage, it.damage + durabilityTaken)
					}
					
					e.affectedBlocks.clear()
					e.affectedEntities.clear()
					
					with(ExplosionBuilder()) {
						this.destroyBlocks = false
						this.damageEntities = false
						clone(explosion, source = null)
					}
					
					break
				}
			}
		}
	}
	
	@SubscribeEvent
	fun onLivingHurt(e: LivingHurtEvent) {
		if (!e.source.isExplosion) {
			return
		}
		
		val entity = e.entityLiving
		val world = entity.world
		
		if (lastRepairMarkTime.get() != world.totalTime || !lastRepairMarkEntities.get().remove(entity.uniqueID)) {
			return
		}
		
		val finalAmount = if (e.source.isDifficultyScaled)
			getNormalDifficultyEquivalentDamage(e.amount, world.difficulty)
		else
			e.amount
		
		if (finalAmount < 50F) {
			return
		}
		
		for(hand in Hand.values()) {
			val heldItem = entity.getHeldItem(hand)
			
			if (heldItem.item === this) {
				heldItem.damage = 0
				// TODO sound fx
			}
		}
	}
}
