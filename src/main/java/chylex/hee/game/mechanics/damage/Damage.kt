package chylex.hee.game.mechanics.damage

import chylex.hee.game.mechanics.damage.IDamageDealer.Companion.CANCEL_DAMAGE
import net.minecraft.entity.Entity

class Damage(private vararg val processors: IDamageProcessor) : IDamageDealer {
	private val properties = DamageProperties().apply {
		val writer = Writer()
		processors.forEach { it.setup(writer) }
	}
	
	private fun dealToInternal(amount: Float, target: Entity, directSource: Entity?, remoteSource: Entity?, damageTitle: String): Boolean {
		val reader = properties.Reader()
		val finalAmount = processors.fold(amount) { acc, processor -> processor.modifyDamage(acc, target, reader) }
		
		if (finalAmount == CANCEL_DAMAGE || !target.attackEntityFrom(properties.createDamageSource(damageTitle, directSource, remoteSource), finalAmount)) {
			return false
		}
		
		processors.forEach { it.afterDamage(target, reader) }
		return true
	}
	
	override fun dealTo(amount: Float, target: Entity, title: String) =
		dealToInternal(amount, target, null, null, title)
	
	override fun dealToFrom(amount: Float, target: Entity, source: Entity, title: String) =
		dealToInternal(amount, target, source, source, title)
	
	override fun dealToIndirectly(amount: Float, target: Entity, directSource: Entity, remoteSource: Entity?, title: String) =
		dealToInternal(amount, target, directSource, remoteSource, title)
	
	
}

