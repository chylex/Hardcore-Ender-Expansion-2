package chylex.hee.game.potion
import chylex.hee.system.migration.Potion
import net.minecraft.potion.EffectInstance

fun Potion.makeEffect(duration: Int): EffectInstance{
	return EffectInstance(this, duration)
}

fun Potion.makeEffect(duration: Int, amplifier: Int): EffectInstance{
	return EffectInstance(this, duration, amplifier)
}

fun Potion.makeEffect(duration: Int, amplifier: Int = 0, isAmbient: Boolean = false, showParticles: Boolean = true): EffectInstance{
	return EffectInstance(this, duration, amplifier, isAmbient, showParticles)
}

fun EffectInstance.clone(): EffectInstance{
	return EffectInstance(this)
}
