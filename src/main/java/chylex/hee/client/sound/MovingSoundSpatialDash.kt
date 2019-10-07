package chylex.hee.client.sound
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.nextFloat
import net.minecraft.client.audio.MovingSound
import net.minecraft.util.SoundCategory

class MovingSoundSpatialDash(private val entity: EntityProjectileSpatialDash) : MovingSound(Sounds.ITEM_ELYTRA_FLYING, SoundCategory.PLAYERS){
	init{
		volume = 0.9F
		pitch = entity.world.rand.nextFloat(1.1F, 1.4F)
		repeat = true
		repeatDelay = 0
	}
	
	override fun update(){
		if (entity.isDead){
			donePlaying = true
			return
		}
		
		xPosF = entity.posX.toFloat()
		yPosF = entity.posY.toFloat()
		zPosF = entity.posZ.toFloat()
	}
}
