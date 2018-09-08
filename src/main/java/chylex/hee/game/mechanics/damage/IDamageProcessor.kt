package chylex.hee.game.mechanics.damage
import net.minecraft.entity.Entity

interface IDamageProcessor{
	fun setup(properties: DamageProperties.Writer){}
	fun modifyDamage(amount: Float, target: Entity, properties: DamageProperties.Reader): Float = amount
	fun afterDamage(target: Entity, properties: DamageProperties.Reader){}
	
	companion object{ // TODO make static fields in kotlin 1.3 and use default methods
		const val CANCEL_DAMAGE = -1F
	}
}
