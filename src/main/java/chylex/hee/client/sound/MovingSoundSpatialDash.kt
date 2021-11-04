package chylex.hee.client.sound

import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.random.nextFloat
import net.minecraft.client.audio.TickableSound
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents

@Sided(Side.CLIENT)
class MovingSoundSpatialDash(private val entity: EntityProjectileSpatialDash) : TickableSound(SoundEvents.ITEM_ELYTRA_FLYING, SoundCategory.PLAYERS) {
	init {
		volume = 0.9F
		pitch = entity.world.rand.nextFloat(1.1F, 1.4F)
		repeat = true
		repeatDelay = 0
	}
	
	override fun tick() {
		if (!entity.isAlive) {
			finishPlaying()
			return
		}
		
		x = entity.posX
		y = entity.posY
		z = entity.posZ
	}
}
