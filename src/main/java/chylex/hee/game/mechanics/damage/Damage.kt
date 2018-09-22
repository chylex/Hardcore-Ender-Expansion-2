package chylex.hee.game.mechanics.damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.CANCEL_DAMAGE
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityThrowable

class Damage(private vararg val processors: IDamageProcessor){
	private val properties = DamageProperties().apply {
		val writer = Writer()
		processors.forEach { it.setup(writer) }
	}
	
	private fun dealToInternal(amount: Float, target: Entity, triggeringSource: Entity?, remoteSource: Entity?, damageTitle: String): Boolean{
		val reader = properties.Reader()
		val finalAmount = processors.fold(amount){ acc, processor -> processor.modifyDamage(acc, target, reader) }
		
		if (finalAmount == CANCEL_DAMAGE || !target.attackEntityFrom(properties.createDamageSource(damageTitle, triggeringSource, remoteSource), finalAmount)){
			return false
		}
		
		processors.forEach { it.afterDamage(target, reader) }
		return true
	}
	
	fun dealTo(amount: Float, target: Entity, title: String = TITLE_GENERIC) =
		dealToInternal(amount, target, null, null, title)
	
	fun dealToFrom(target: Entity, source: EntityLivingBase, title: String = determineTitleDirect(source)) =
		dealToInternal(source.getEntityAttribute(ATTACK_DAMAGE).attributeValue.toFloat(), target, source, source, title)
	
	fun dealToFrom(amount: Float, target: Entity, source: Entity, title: String = determineTitleDirect(source)) =
		dealToInternal(amount, target, source, source, title)
	
	fun dealToIndirectly(amount: Float, target: Entity, triggeringSource: Entity, remoteSource: Entity, title: String = determineTitleIndirect(triggeringSource, remoteSource)) =
		dealToInternal(amount, target, triggeringSource, remoteSource, title)
	
	companion object{
		const val TITLE_GENERIC = "generic"
		const val TITLE_PLAYER  = "player"
		const val TITLE_MOB     = "mob"
		const val TITLE_THROWN  = "thrown"
		
		const val TITLE_MAGIC   = "magic"
		const val TITLE_IN_FIRE = "inFire"
		
		private fun determineTitleDirect(source: Entity): String = when(source){
			is EntityPlayer -> TITLE_PLAYER
			else            -> TITLE_MOB
		}
		
		private fun determineTitleIndirect(triggeringSource: Entity, remoteSource: Entity) = when(triggeringSource){
			is EntityThrowable -> TITLE_THROWN
			else               -> determineTitleDirect(remoteSource)
		}
	}
}
