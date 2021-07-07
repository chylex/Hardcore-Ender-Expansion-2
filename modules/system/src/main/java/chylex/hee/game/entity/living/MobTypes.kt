package chylex.hee.game.entity.living

import net.minecraft.entity.LivingEntity

object MobTypes {
	fun isBoss(entity: LivingEntity): Boolean {
		return !entity.canChangeDimension() // TODO better impl?
	}
}
