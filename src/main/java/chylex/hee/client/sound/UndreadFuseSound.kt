package chylex.hee.client.sound

import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.init.ModSounds
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.random.nextFloat
import net.minecraft.client.audio.ISound.AttenuationType
import net.minecraft.client.audio.TickableSound
import net.minecraft.util.SoundCategory

@Sided(Side.CLIENT)
class UndreadFuseSound(private val undread: EntityMobUndread) : TickableSound(ModSounds.MOB_UNDREAD_FUSE, SoundCategory.HOSTILE) {
	init {
		attenuationType = AttenuationType.LINEAR
		repeat = true
		repeatDelay = 0
		volume = 1F
		pitch = undread.rng.nextFloat(0.6F, 1.2F)
	}
	
	override fun tick() {
		if (undread.isAlive) {
			x = undread.posX
			y = undread.posY
			z = undread.posZ
		}
		else {
			finishPlaying()
		}
	}
}
