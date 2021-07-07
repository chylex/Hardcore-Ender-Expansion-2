package chylex.hee.game.potion

import chylex.hee.HEE
import chylex.hee.game.potion.brewing.PotionTypeMap
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.SubscribeAllEvents
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.attributes.AttributeModifierManager
import net.minecraft.potion.AttackDamageEffect
import net.minecraft.potion.Effect
import net.minecraft.potion.EffectInstance
import net.minecraft.potion.EffectType.HARMFUL
import net.minecraft.potion.HealthBoostEffect

@SubscribeAllEvents(modid = HEE.ID)
object CorruptionEffect : Effect(HARMFUL, RGB(1, 1, 1).i) {
	val TYPE
		get() = PotionTypeMap.getPotion(this)
	
	private val pauseAttributeSkipping = ThreadLocal.withInitial { false }
	
	@JvmStatic
	fun shouldCorrupt(effect: Effect, entity: LivingEntity): Boolean {
		return effect.isBeneficial && entity.isPotionActive(CorruptionEffect) // CorruptionEffect being set as harmful also prevents infinite loop from isPotionActive
	}
	
	@JvmStatic
	fun shouldCorrupt(effect: EffectInstance, entity: LivingEntity): Boolean {
		return effect.potion.isBeneficial && entity.isPotionActive(CorruptionEffect)
	}
	
	@JvmStatic
	fun shouldSkipAttributeChange(effect: Effect, entity: LivingEntity): Boolean {
		return !pauseAttributeSkipping.get() && canCorruptAttributes(effect) && entity.isPotionActive(CorruptionEffect)
	}
	
	private fun canCorruptAttributes(effect: Effect): Boolean {
		// TODO skip corrupting attributes on overriding classes, currently it doesn't work with Absorption and could mess with modded ones that do similar things
		return effect.isBeneficial && effect.javaClass.let { it === Effect::class.java || it === AttackDamageEffect::class.java || it === HealthBoostEffect::class.java }
	}
	
	private inline fun processCurrentlyRunningPotions(entity: LivingEntity, callback: (EffectInstance) -> Unit) {
		pauseAttributeSkipping.set(true)
		
		for ((potion, effect) in entity.activePotionMap) {
			if (canCorruptAttributes(potion)) {
				callback(effect)
			}
		}
		
		pauseAttributeSkipping.set(false)
	}
	
	override fun applyAttributesModifiersToEntity(entity: LivingEntity, attributes: AttributeModifierManager, amplifier: Int) {
		processCurrentlyRunningPotions(entity) { it.potion.removeAttributesModifiersFromEntity(entity, attributes, it.amplifier) }
	}
	
	override fun removeAttributesModifiersFromEntity(entity: LivingEntity, attributes: AttributeModifierManager, amplifier: Int) {
		processCurrentlyRunningPotions(entity) { it.potion.applyAttributesModifiersToEntity(entity, attributes, it.amplifier) }
	}
}
