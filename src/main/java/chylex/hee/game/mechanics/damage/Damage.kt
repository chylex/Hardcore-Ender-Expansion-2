package chylex.hee.game.mechanics.damage

import chylex.hee.game.mechanics.damage.IDamageDealer.Companion.CANCEL_DAMAGE
import chylex.hee.system.math.toRadians
import chylex.hee.system.migration.EntityLivingBase
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK
import kotlin.math.cos
import kotlin.math.sin

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
		
		if (directSource is EntityLivingBase && target is LivingEntity) {
			val extraKnockback = directSource.getAttributeValue(ATTACK_KNOCKBACK) + EnchantmentHelper.getKnockbackModifier(directSource)
			if (extraKnockback > 0F) {
				val yawRad = directSource.rotationYaw.toDouble().toRadians()
				target.applyKnockback(extraKnockback.toFloat() * 0.5F, sin(yawRad), -cos(yawRad))
				directSource.motion = directSource.motion.mul(0.6, 1.0, 0.6)
			}
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

