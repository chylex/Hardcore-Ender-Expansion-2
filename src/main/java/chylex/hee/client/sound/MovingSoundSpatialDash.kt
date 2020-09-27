package chylex.hee.client.sound
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.system.migration.Sounds
import chylex.hee.system.random.nextFloat
import net.minecraft.client.audio.TickableSound
import net.minecraft.util.SoundCategory

class MovingSoundSpatialDash(private val entity: EntityProjectileSpatialDash) : TickableSound(Sounds.ITEM_ELYTRA_FLYING, SoundCategory.PLAYERS){
	init{
		volume = 0.9F
		pitch = entity.world.rand.nextFloat(1.1F, 1.4F)
		repeat = true
		repeatDelay = 0
	}
	
	override fun tick(){
		if (!entity.isAlive){
			donePlaying = true
			return
		}
		
		x = entity.posX.toFloat()
		y = entity.posY.toFloat()
		z = entity.posZ.toFloat()
	}
}
