package chylex.hee.game.potion.util

import net.minecraft.potion.Effect
import net.minecraft.potion.EffectInstance

fun Effect.makeInstance(duration: Int): EffectInstance {
	return EffectInstance(this, duration)
}

fun Effect.makeInstance(duration: Int, amplifier: Int): EffectInstance {
	return EffectInstance(this, duration, amplifier)
}

fun Effect.makeInstance(duration: Int, amplifier: Int = 0, isAmbient: Boolean = false, showParticles: Boolean = true): EffectInstance {
	return EffectInstance(this, duration, amplifier, isAmbient, showParticles)
}

fun EffectInstance.clone(): EffectInstance {
	return EffectInstance(this)
}
