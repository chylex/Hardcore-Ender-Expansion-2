package chylex.hee.system.util
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect

fun Potion.makeEffect(duration: Int): PotionEffect{
	return PotionEffect(this, duration)
}

fun Potion.makeEffect(duration: Int, amplifier: Int): PotionEffect{
	return PotionEffect(this, duration, amplifier)
}

fun Potion.makeEffect(duration: Int, amplifier: Int = 0, isAmbient: Boolean = false, showParticles: Boolean = true): PotionEffect{
	return PotionEffect(this, duration, amplifier, isAmbient, showParticles)
}
