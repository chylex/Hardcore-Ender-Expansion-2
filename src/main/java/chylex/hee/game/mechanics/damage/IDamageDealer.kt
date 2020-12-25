package chylex.hee.game.mechanics.damage

import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntityThrowable
import net.minecraft.entity.Entity
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE

interface IDamageDealer {
	fun dealTo(amount: Float, target: Entity, title: String = TITLE_GENERIC): Boolean
	fun dealToFrom(amount: Float, target: Entity, source: Entity, title: String = determineTitleDirect(source)): Boolean
	fun dealToIndirectly(amount: Float, target: Entity, directSource: Entity, remoteSource: Entity?, title: String = determineTitleIndirect(directSource, remoteSource)): Boolean
	
	@JvmDefault
	fun dealToFrom(target: Entity, source: EntityLivingBase, title: String = determineTitleDirect(source)): Boolean {
		return dealToFrom(source.getAttribute(ATTACK_DAMAGE).value.toFloat(), target, source, title)
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
		
		fun determineTitleDirect(source: Entity?) = when(source) {
			is EntityPlayer -> TITLE_PLAYER
			null            -> TITLE_GENERIC
			else            -> TITLE_MOB
		}
		
		fun determineTitleIndirect(directSource: Entity, remoteSource: Entity?) = when(directSource) {
			is EntityThrowable -> TITLE_THROWN
			else               -> determineTitleDirect(remoteSource)
		}
	}
}
