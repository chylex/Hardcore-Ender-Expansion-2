package chylex.hee.game.entity.damage

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ThrowableEntity

interface IDamageDealer {
	fun dealTo(amount: Float, target: Entity, title: String = TITLE_GENERIC): Boolean
	fun dealToFrom(amount: Float, target: Entity, source: Entity, title: String = determineTitleDirect(source)): Boolean
	fun dealToIndirectly(amount: Float, target: Entity, directSource: Entity, remoteSource: Entity?, title: String = determineTitleIndirect(directSource, remoteSource)): Boolean
	
	fun dealToFrom(target: Entity, source: LivingEntity, title: String = determineTitleDirect(source)): Boolean {
		return dealToFrom(source.getAttributeValue(ATTACK_DAMAGE).toFloat(), target, source, title)
	}
	
	companion object {
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
		
		fun determineTitleDirect(source: Entity?) = when (source) {
			is PlayerEntity -> TITLE_PLAYER
			null            -> TITLE_GENERIC
			else            -> TITLE_MOB
		}
		
		fun determineTitleIndirect(directSource: Entity, remoteSource: Entity?) = when (directSource) {
			is ThrowableEntity -> TITLE_THROWN
			else               -> determineTitleDirect(remoteSource)
		}
	}
}
