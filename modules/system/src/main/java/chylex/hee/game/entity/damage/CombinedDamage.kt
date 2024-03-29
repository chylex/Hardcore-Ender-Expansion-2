package chylex.hee.game.entity.damage

import chylex.hee.game.entity.damage.IDamageDealer.Companion.TITLE_GENERIC
import chylex.hee.game.entity.damage.IDamageDealer.Companion.determineTitleDirect
import chylex.hee.game.entity.damage.IDamageDealer.Companion.determineTitleIndirect
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity

class CombinedDamage(private vararg val definitions: Pair<IDamageDealer, Float>) {
	private fun shouldPreventDamage(target: Entity): Boolean {
		if (target is LivingEntity && target.hurtResistantTime > (target.maxHurtResistantTime / 2F)) {
			val lastDamage = target.lastDamage
			
			if (definitions.all { it.second <= lastDamage }) {
				return true
			}
		}
		
		return false
	}
	
	private inline fun dealToInternal(target: Entity, damageCall: (Pair<IDamageDealer, Float>) -> Boolean): Boolean {
		if (shouldPreventDamage(target)) {
			return false
		}
		
		var causedAnyDamage = false
		var finalHurtResistantTime = target.hurtResistantTime
		
		for (definition in definitions) {
			if (damageCall(definition)) {
				causedAnyDamage = true
				finalHurtResistantTime = target.hurtResistantTime
				target.hurtResistantTime = 0
			}
		}
		
		target.hurtResistantTime = finalHurtResistantTime
		return causedAnyDamage
	}
	
	fun dealTo(target: Entity, title: String = TITLE_GENERIC) =
		dealToInternal(target) { (damage, amount) -> damage.dealTo(amount, target, title) }
	
	fun dealToFrom(target: Entity, source: Entity, title: String = determineTitleDirect(source)) =
		dealToInternal(target) { (damage, amount) -> damage.dealToFrom(amount, target, source, title) }
	
	fun dealToIndirectly(target: Entity, triggeringSource: Entity, remoteSource: Entity, title: String = determineTitleIndirect(triggeringSource, remoteSource)) =
		dealToInternal(target) { (damage, amount) -> damage.dealToIndirectly(amount, target, triggeringSource, remoteSource, title) }
}
