package chylex.hee.game.mechanics.damage
import chylex.hee.game.mechanics.damage.Damage.Companion.TITLE_GENERIC
import net.minecraft.entity.Entity

class CombinedDamage(private vararg val definitions: Pair<Damage, Float>){
	private fun dealToInternal(target: Entity, damageCall: (Pair<Damage, Float>) -> Boolean): Boolean{
		var causedAnyDamage = false
		var finalHurtResistantTime = target.hurtResistantTime
		
		for(definition in definitions){
			if (damageCall(definition)){
				causedAnyDamage = true
				finalHurtResistantTime = target.hurtResistantTime
				target.hurtResistantTime = 0
			}
		}
		
		target.hurtResistantTime = finalHurtResistantTime
		return causedAnyDamage
	}
	
	fun dealTo(target: Entity, title: String = TITLE_GENERIC) =
		dealToInternal(target){ (damage, amount) -> damage.dealTo(amount, target, title) }
	
	fun dealToFrom(target: Entity, source: Entity, title: String = Damage.determineTitleDirect(source)) =
		dealToInternal(target){ (damage, amount) -> damage.dealToFrom(amount, target, source, title) }
	
	fun dealToIndirectly(target: Entity, triggeringSource: Entity, remoteSource: Entity, title: String = Damage.determineTitleIndirect(triggeringSource, remoteSource)) =
		dealToInternal(target){ (damage, amount) -> damage.dealToIndirectly(amount, target, triggeringSource, remoteSource, title) }
}
