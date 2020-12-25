package chylex.hee.game.mechanics.damage.special

import chylex.hee.game.mechanics.damage.IDamageDealer
import net.minecraft.entity.Entity

class FallbackDamage(private vararg val definitions: Pair<IDamageDealer, Double>) : IDamageDealer {
	private inline fun dealToInternal(amount: Float, damageCall: (IDamageDealer, Float) -> Boolean): Boolean {
		for(definition in definitions) {
			if (damageCall(definition.first, (amount * definition.second).toFloat())) {
				return true
			}
		}
		
		return false
	}
	
	override fun dealTo(amount: Float, target: Entity, title: String) =
		dealToInternal(amount) { damage, adjustedAmount -> damage.dealTo(adjustedAmount, target, title) }
	
	override fun dealToFrom(amount: Float, target: Entity, source: Entity, title: String) =
		dealToInternal(amount) { damage, adjustedAmount -> damage.dealToFrom(adjustedAmount, target, source, title) }
	
	override fun dealToIndirectly(amount: Float, target: Entity, directSource: Entity, remoteSource: Entity?, title: String) =
		dealToInternal(amount) { damage, adjustedAmount -> damage.dealToIndirectly(adjustedAmount, target, directSource, remoteSource, title) }
}
