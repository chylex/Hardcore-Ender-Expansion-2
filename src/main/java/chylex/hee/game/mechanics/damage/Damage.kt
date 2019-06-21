package chylex.hee.game.mechanics.damage
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
	
	private fun dealToInternal(amount: Float, target: Entity, directSource: Entity?, remoteSource: Entity?, damageTitle: String): Boolean{
		val reader = properties.Reader()
		val finalAmount = processors.fold(amount){ acc, processor -> processor.modifyDamage(acc, target, reader) }
		
		if (finalAmount == CANCEL_DAMAGE || !target.attackEntityFrom(properties.createDamageSource(damageTitle, directSource, remoteSource), finalAmount)){
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
	
	fun dealToIndirectly(amount: Float, target: Entity, directSource: Entity, remoteSource: Entity?, title: String = determineTitleIndirect(directSource, remoteSource)) =
		dealToInternal(amount, target, directSource, remoteSource, title)
	
	companion object{
		const val CANCEL_DAMAGE = 0F
		
		const val TITLE_GENERIC = "generic"
		const val TITLE_PLAYER  = "player"
		const val TITLE_MOB     = "mob"
		const val TITLE_THROWN  = "thrown"
		
		const val TITLE_MAGIC         = "magic"
		const val TITLE_FALL          = "fall"
		const val TITLE_STARVE        = "starve"
		const val TITLE_WITHER        = "wither"
		const val TITLE_IN_FIRE       = "inFire"
		const val TITLE_FALLING_BLOCK = "fallingBlock"
		
		fun determineTitleDirect(source: Entity?) = when(source){
			is EntityPlayer -> TITLE_PLAYER
			null            -> TITLE_GENERIC
			else            -> TITLE_MOB
		}
		
		fun determineTitleIndirect(directSource: Entity, remoteSource: Entity?) = when(directSource){
			is EntityThrowable -> TITLE_THROWN
			else               -> determineTitleDirect(remoteSource)
		}
	}
}

